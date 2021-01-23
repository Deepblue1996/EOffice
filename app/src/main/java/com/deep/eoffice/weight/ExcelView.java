package com.deep.eoffice.weight;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.deep.dpwork.util.DisplayUtil;
import com.deep.dpwork.util.DoubleUtil;
import com.deep.dpwork.util.Lag;
import com.deep.eoffice.data.ColChild;
import com.deep.eoffice.data.RowChild;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class - ExcelView - 多功能表格控件
 * <p>
 * Created by Deepblue on 2021/1/22 0029.
 */
public class ExcelView extends View {

    // 每次扩展多少次
    private final int KUO_SIZE = 5;

    // 标准列宽度
    private double COL_WIDTH = 150;
    // 标准行宽度
    private double ROW_HEIGHT = 80;

    // 滑动条宽度
    private double BAR_WIDTH = 0;
    // 整个表格宽度
    private double TABLE_WIDTH = 0;
    // 整个表格高度
    private double TABLE_HEIGHT = 0;
    // 整个表格列数目
    private int TABLE_COL_SIZE = 0;
    // 整个表格行数目
    private int TABLE_ROW_SIZE = 0;

    // 滑动条宽度
    private double DATA_BAR_WIDTH = 0;
    // 整个表格宽度
    private double DATA_TABLE_WIDTH = 0;
    // 整个表格高度
    private double DATA_TABLE_HEIGHT = 0;
    // 整个表格列数目
    private int DATA_TABLE_COL_SIZE = 0;
    // 整个表格行数目
    private int DATA_TABLE_ROW_SIZE = 0;

    // 画笔
    private Paint paintBar;
    private Paint paintBorder;
    private Paint paintExtendBorder;
    private Paint paintText;
    // 选中
    private Paint paintTextSelect;
    private Paint paintSelectBorder;

    private Context mContext;

    private double bigWidth;
    private double bigHeight;

    private boolean hasInit = false;

    // 绘画范围
    private List<ColChild> excelDataTable = new ArrayList<>();
    // 实际范围
    private List<ColChild> excelData = new ArrayList<>();

    // 选择
    private List<Integer> selectList = new ArrayList<>();

    // 点击触发的单元
    private Point nowSelectDown = new Point(-1, -1);

    // 放开触发的单元
    private Point nowSelectUp = new Point(-2, -2);

    // 选中的参数
    private RectF selectTxy = new RectF();

    // 触摸辅助变量
    private float tableXDown = 0;
    private float tableYDown = 0;
    private float tableXMove = 0;
    private float tableYMove = 0;

    // 点击时间
    private long tableDownTime = 0;

    // 世界坐标
    private float tableX = 0;
    private float tableY = 0;

    // 临时扩展列数量
    private int xExtendNum = 0;
    // 临时扩展行数量
    private int yExtendNum = 0;

    // 放大手势
    private ScaleGestureDetector scaleGestureDetector;

    public ExcelView(Context context) {
        super(context);
        initView(context);
    }

    public ExcelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ExcelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.mContext = context;

        BAR_WIDTH = DisplayUtil.dip2px(mContext, 4);

        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintBorder.setDither(true);
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeCap(Paint.Cap.ROUND);
        paintBorder.setStrokeWidth(DisplayUtil.dip2px(mContext, 1));
        paintBorder.setColor(Color.parseColor("#FF6666"));

        paintExtendBorder = new Paint();
        paintExtendBorder.setAntiAlias(true);
        paintExtendBorder.setDither(true);
        paintExtendBorder.setStyle(Paint.Style.STROKE);
        paintExtendBorder.setStrokeCap(Paint.Cap.ROUND);
        paintExtendBorder.setStrokeWidth(DisplayUtil.dip2px(mContext, 1));
        paintExtendBorder.setColor(Color.parseColor("#F0F0F0"));

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setDither(true);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setStrokeCap(Paint.Cap.ROUND);
        paintText.setColor(Color.parseColor("#666699"));
        paintText.setTextSize(35);

        paintBar = new Paint();
        paintBar.setAntiAlias(true);
        paintBar.setDither(true);
        paintBar.setStyle(Paint.Style.FILL);
        paintBar.setStrokeCap(Paint.Cap.ROUND);
        paintBar.setColor(Color.parseColor("#999999"));
        paintBar.setAlpha(0);

        paintTextSelect = new Paint();
        paintTextSelect.setAntiAlias(true);
        paintTextSelect.setDither(true);
        paintTextSelect.setStyle(Paint.Style.FILL);
        paintTextSelect.setStrokeCap(Paint.Cap.ROUND);
        paintTextSelect.setTextSize(35);
        paintTextSelect.setColor(Color.parseColor("#ff0000"));
        paintTextSelect.setTypeface(Typeface.DEFAULT_BOLD);

        paintSelectBorder = new Paint();
        paintSelectBorder.setAntiAlias(true);
        paintSelectBorder.setDither(true);
        paintSelectBorder.setStyle(Paint.Style.STROKE);
        paintSelectBorder.setStrokeCap(Paint.Cap.ROUND);
        paintSelectBorder.setStrokeWidth(DisplayUtil.dip2px(mContext, 2));
        paintSelectBorder.setColor(Color.parseColor("#0000FF"));

        scaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGesture());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, (float) bigWidth, (float) bigHeight, paintBorder);

        // --------------------------------------------------------------------------------------------
        // 背景层
        int width = 0;
        for (int i = 0; i < excelDataTable.size(); i++) {
            width += excelDataTable.get(i).getWidth();
            int height = 0;
            for (int j = 0; j < excelDataTable.get(i).getRowChild().size(); j++) {
                height += excelDataTable.get(i).getRowChild().get(j).getHeight();

                double gx = width - excelDataTable.get(i).getWidth() + tableX + tableXMove;
                double gy = height - excelDataTable.get(i).getRowChild().get(j).getHeight() + tableY + tableYMove;
                double gr = width + tableX + tableXMove;
                double gb = height + tableY + tableYMove;

                // 只显示控件范围
                if (gx > -excelDataTable.get(i).getWidth() && gx < bigWidth + excelDataTable.get(i).getWidth() &&
                        gy > -excelDataTable.get(i).getRowChild().get(j).getHeight() && gy < bigHeight + excelDataTable.get(i).getRowChild().get(j).getHeight()) {

                    // 绘制每个单元格
                    if (i < DATA_TABLE_COL_SIZE && j < DATA_TABLE_ROW_SIZE) {
                        canvas.drawRect((float) gx, (float) gy, (float) gr, (float) gb, paintBorder);
                    } else {
                        canvas.drawRect((float) gx, (float) gy, (float) gr, (float) gb, paintExtendBorder);
                    }

                    // 赋予大小
                    paintText.setTextSize(excelDataTable.get(i).getRowChild().get(j).getTextSize());

                    @SuppressLint("DrawAllocation") Rect rect = new Rect();
                    paintText.getTextBounds(excelDataTable.get(i).getRowChild().get(j).getChildData(),
                            0, excelDataTable.get(i).getRowChild().get(j).getChildData().length(), rect);
                    int tx = (int) (width - excelDataTable.get(i).getWidth() / 2 - rect.width() / 2 + tableX + tableXMove);
                    int ty = (int) (height - excelDataTable.get(i).getRowChild().get(j).getHeight() / 2 + rect.height() / 2 + tableY + tableYMove);

                    // 选中显示
                    if (!(nowSelectDown.x == nowSelectUp.x && nowSelectDown.y == nowSelectUp.y && i == nowSelectDown.x && j == nowSelectDown.y)) {
                        canvas.drawText(excelDataTable.get(i).getRowChild().get(j).getChildData(), tx, ty, paintText);
                    }
                }
            }
        }

        // --------------------------------------------------------------------------------------------
        // 样式选中层
        width = 0;
        for (int i = 0; i < excelDataTable.size(); i++) {
            width += excelDataTable.get(i).getWidth();
            int height = 0;
            for (int j = 0; j < excelDataTable.get(i).getRowChild().size(); j++) {
                height += excelDataTable.get(i).getRowChild().get(j).getHeight();

                double gx = width - excelDataTable.get(i).getWidth() + tableX + tableXMove;
                double gy = height - excelDataTable.get(i).getRowChild().get(j).getHeight() + tableY + tableYMove;
                double gr = width + tableX + tableXMove;
                double gb = height + tableY + tableYMove;

                // 只显示控件范围
                if (gx > -excelDataTable.get(i).getWidth() && gx < bigWidth + excelDataTable.get(i).getWidth() &&
                        gy > -excelDataTable.get(i).getRowChild().get(j).getHeight() && gy < bigHeight + excelDataTable.get(i).getRowChild().get(j).getHeight()) {

                    // 赋予大小
                    paintTextSelect.setTextSize(excelDataTable.get(i).getRowChild().get(j).getTextSize());

                    @SuppressLint("DrawAllocation") Rect rect = new Rect();
                    paintTextSelect.getTextBounds(excelDataTable.get(i).getRowChild().get(j).getChildData(),
                            0, excelDataTable.get(i).getRowChild().get(j).getChildData().length(), rect);
                    int tx = (int) (width - excelDataTable.get(i).getWidth() / 2 - rect.width() / 2 + tableX + tableXMove);
                    int ty = (int) (height - excelDataTable.get(i).getRowChild().get(j).getHeight() / 2 + rect.height() / 2 + tableY + tableYMove);

                    // 选中显示
                    if (nowSelectDown.x == nowSelectUp.x && nowSelectDown.y == nowSelectUp.y && i == nowSelectDown.x && j == nowSelectDown.y) {
                        canvas.drawText(excelDataTable.get(i).getRowChild().get(j).getChildData(), tx, ty, paintTextSelect);
                        selectTxy.set((float) gx, (float) gy, (float) gr, (float) gb);
                        canvas.drawRect(selectTxy, paintSelectBorder);
                    }
                }
            }
        }

        // --------------------------------------------------------------------------------------------
        // 互动条层

        double wBai = (bigWidth - DisplayUtil.dip2px(mContext, 6)) / (double) TABLE_WIDTH * bigWidth;
        double hBai = (bigHeight - DisplayUtil.dip2px(mContext, 6)) / (double) TABLE_HEIGHT * bigHeight;

        double xBarMove = (-(tableX + tableXMove)) / TABLE_WIDTH * (double) (bigWidth - DisplayUtil.dip2px(mContext, 6));
        double yBarMove = (-(tableY + tableYMove)) / TABLE_HEIGHT * (double) (bigHeight - DisplayUtil.dip2px(mContext, 6));

        //Lag.i("滑动块 xBarMove:" + xBarMove + " yBarMove:" + yBarMove);

        // 横向
        canvas.drawRoundRect((float) xBarMove + DisplayUtil.dip2px(mContext, 3),
                (float) bigHeight - (float) BAR_WIDTH - DisplayUtil.dip2px(mContext, 3),
                (float) wBai + (float) xBarMove,
                (float) bigHeight - DisplayUtil.dip2px(mContext, 3),
                (float) BAR_WIDTH, (float) BAR_WIDTH, paintBar);
        // 竖向
        canvas.drawRoundRect((float) bigWidth - (float) BAR_WIDTH - DisplayUtil.dip2px(mContext, 3),
                (float) yBarMove + DisplayUtil.dip2px(mContext, 3),
                (float) bigWidth - DisplayUtil.dip2px(mContext, 3),
                (float) hBai + (float) yBarMove,
                (float) BAR_WIDTH, (float) BAR_WIDTH, paintBar);

        // --------------------------------------------------------------------------------------------
    }

    private Timer animTimer;
    private Timer animEndTimer;
    private final float[] vaAlpha = {0};

    /**
     * 互动条动画
     *
     * @param timeSecond
     * @param show
     */
    private void startShowBar(int timeSecond, boolean show) {

        float value = 1000.0f * timeSecond / 24 / 255;

        if (animTimer != null) {
            animTimer.cancel();
            animTimer = null;
        }
        if (show) {
            vaAlpha[0] = 0;
        } else {
            vaAlpha[0] = 255;
        }
        paintBar.setAlpha((int) vaAlpha[0]);
        invalidate();

        animTimer = new Timer();
        animTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                post(() -> {
                    if (show) {
                        vaAlpha[0] += value;
                    } else {
                        vaAlpha[0] -= value;
                    }
                    paintBar.setAlpha((int) vaAlpha[0]);
                    invalidate();
                    if (vaAlpha[0] > 255) {
                        if (animTimer != null) {
                            animTimer.cancel();
                            animTimer = null;
                        }
                    } else if (vaAlpha[0] < 0) {
                        if (animTimer != null) {
                            animTimer.cancel();
                            animTimer = null;
                        }
                    }
                });
            }
        }, (int) value, (int) value);
    }

    // 滑动动画
    private ValueAnimator valueAnimatorX;
    private ValueAnimator valueAnimatorY;

    /**
     * 滑动动画
     *
     * @param xSpanTemp  x距离
     * @param ySpanTemp  y距离
     * @param timeSecond 毫秒
     * @param haveTime   秒
     */
    private void slidingToScreenXY(float xSpanTemp, float ySpanTemp, float timeSecond, float haveTime) {

        float xSpan = xSpanTemp * (300 / timeSecond) * 2;
        float ySpan = ySpanTemp * (300 / timeSecond) * 2;

        if (xSpan > Math.abs(tableX)) {
            xSpan = Math.abs(tableX);
        }
        if (ySpan > Math.abs(tableY)) {
            ySpan = Math.abs(tableY);
        }

        if (xSpan < 0 && Math.abs(xSpan) > TABLE_WIDTH - bigWidth) {
            xSpan = -(float) (TABLE_WIDTH - bigWidth);
        }
        if (ySpan < 0 && Math.abs(ySpan) > TABLE_HEIGHT - bigHeight) {
            ySpan = -(float) (TABLE_HEIGHT - bigHeight);
        }

        valueAnimatorX = ValueAnimator.ofFloat(tableX, tableX + xSpan);
        valueAnimatorX.addUpdateListener(animation -> {
            tableX = (float) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimatorX.setInterpolator(new DecelerateInterpolator());
        valueAnimatorX.setDuration((long) (haveTime * 1000));

        valueAnimatorY = ValueAnimator.ofFloat(tableY, tableY + ySpan);
        valueAnimatorY.addUpdateListener(animation -> {
            tableY = (float) animation.getAnimatedValue();
            invalidate();
        });
        valueAnimatorY.setInterpolator(new DecelerateInterpolator());
        valueAnimatorY.setDuration((long) (haveTime * 1000));

        if (bigWidth > TABLE_WIDTH) {
            if (bigHeight < TABLE_HEIGHT) {
                valueAnimatorY.start();
            }
        } else {
            valueAnimatorY.start();
        }
        if (bigHeight > TABLE_HEIGHT) {
            if (bigWidth < TABLE_WIDTH) {
                valueAnimatorX.start();
            }
        } else {
            valueAnimatorX.start();
        }
    }

    /**
     * 停止动画
     */
    private void stopSliding() {
        if (valueAnimatorX != null && valueAnimatorX.isRunning()) {
            valueAnimatorX.cancel();
        }
        if (valueAnimatorY != null && valueAnimatorY.isRunning()) {
            valueAnimatorY.cancel();
        }
    }

    /**
     * 单元格重新对齐
     *
     * @param excelDataTemp
     * @return
     */
    private List<ColChild> reInitWidthHeight(List<ColChild> excelDataTemp) {
        long startTime = System.currentTimeMillis();
        List<ColChild> excelData = new ArrayList<>(excelDataTemp);
        // 校验每一列行的数量对齐
        int maxRow = 0;
        for (int i = 0; i < excelData.size(); i++) {
            if (maxRow < excelData.get(i).getRowChild().size()) {
                maxRow = excelData.get(i).getRowChild().size();
            }
        }
        // 数量对齐
        for (int i = 0; i < excelData.size(); i++) {
            int nRow = excelData.get(i).getRowChild().size() - maxRow;
            if (nRow != 0) {
                for (int j = 0; j < nRow; j++) {
                    excelData.get(i).getRowChild().add(new RowChild());
                }
            }
        }
        // 行高对齐最高
        for (int j = 0; j < maxRow; j++) {
            // 获取最大值
            float maxHeight = 0;
            for (int i = 0; i < excelData.size(); i++) {
                if (maxHeight < excelData.get(i).getRowChild().get(j).getHeight()) {
                    maxHeight = (float) excelData.get(i).getRowChild().get(j).getHeight();
                }
            }
            // 设置最高高度
            for (int i = 0; i < excelData.size(); i++) {
                excelData.get(i).getRowChild().get(j).setHeight(maxHeight);
            }
        }

        Lag.i("对齐优化并数据统计，耗时:" + (System.currentTimeMillis() - startTime) + "ms");

        return excelData;
    }

    /**
     * 计算表格全部
     */
    private void runTable() {
        // 计算其他数据
        TABLE_WIDTH = 0;
        TABLE_COL_SIZE = 0;
        for (int i = 0; i < excelDataTable.size(); i++) {
            TABLE_WIDTH += excelDataTable.get(i).getWidth();
        }
        TABLE_COL_SIZE = excelDataTable.size();

        TABLE_HEIGHT = 0;
        TABLE_ROW_SIZE = 0;
        if (excelDataTable.size() > 0) {
            for (int i = 0; i < excelDataTable.get(0).getRowChild().size(); i++) {
                TABLE_HEIGHT += excelDataTable.get(0).getRowChild().get(i).getHeight();
            }
            TABLE_ROW_SIZE = excelDataTable.get(0).getRowChild().size();
        }
    }

    /**
     * 计算表格实际
     */
    private void runTableData() {
        // 计算其他数据
        DATA_TABLE_WIDTH = 0;
        DATA_TABLE_COL_SIZE = 0;
        for (int i = 0; i < excelData.size(); i++) {
            DATA_TABLE_WIDTH += excelData.get(i).getWidth();
        }
        DATA_TABLE_COL_SIZE = excelData.size();

        DATA_TABLE_HEIGHT = 0;
        DATA_TABLE_ROW_SIZE = 0;
        if (excelData.size() > 0) {
            for (int i = 0; i < excelData.get(0).getRowChild().size(); i++) {
                DATA_TABLE_HEIGHT += excelData.get(0).getRowChild().get(i).getHeight();
            }
            DATA_TABLE_ROW_SIZE = excelData.get(0).getRowChild().size();
        }
    }

    /**
     * 数据赋予，并对齐数据尺寸
     *
     * @param excelData2 数据
     */
    public void setExcelData(List<ColChild> excelData2) {
        this.excelData = reInitWidthHeight(excelData2);
        // 获取实际测量
        runTableData();

        // 绘画范围
        excelDataTable.clear();
//        excelDataTable.addAll(excelData);

        // 修复数据影响
        for (int i = 0; i < excelData.size(); i++) {
            ColChild colChild = new ColChild();
            colChild.setWidth(excelData.get(i).getWidth());
            colChild.getRowChild().clear();
            for (int j = 0; j < excelData.get(i).getRowChild().size(); j++) {
                RowChild rowChild = new RowChild(
                        excelData.get(i).getRowChild().get(j).getHeight(),
                        excelData.get(i).getRowChild().get(j).getChildData(),
                        excelData.get(i).getRowChild().get(j).getTextSize(),
                        excelData.get(i).getRowChild().get(j).getTextColor());
                colChild.getRowChild().add(rowChild);
            }
            excelDataTable.add(colChild);
        }

        // 获取表格测量
        runTable();

        // excelDataTable.get(0).getRowChild().get(0).setChildData("1312");

        invalidate();
    }

    /**
     * 临时扩展五列
     */
    private void extendWidth() {
        for (int i = 0; i < KUO_SIZE; i++) {
            ColChild colChild = new ColChild();
            if (excelDataTable.size() > 0) {
                for (int j = 0; j < excelDataTable.get(0).getRowChild().size(); j++) {
                    RowChild rowChild = new RowChild(
                            excelDataTable.get(0).getRowChild().get(j).getHeight(),
                            "",
                            excelDataTable.get(0).getRowChild().get(j).getTextSize(),
                            excelDataTable.get(0).getRowChild().get(j).getTextColor());
                    colChild.getRowChild().add(rowChild);
                    colChild.setWidth(COL_WIDTH);
                }
            }
            excelDataTable.add(colChild);
        }
        invalidate();
        xExtendNum++;
    }

    /**
     * 取消一级扩展列
     */
    private void narrowWidth() {
        if (xExtendNum < 1) {
            return;
        }
        for (int i = 0; i < KUO_SIZE; i++) {
            excelDataTable.remove(excelDataTable.size() - 1);
        }
        invalidate();
        xExtendNum--;
    }

    /**
     * 临时扩展五行
     */
    private void extendHeight() {
        for (int i = 0; i < KUO_SIZE; i++) {
            RowChild rowChild = new RowChild();
            if (excelDataTable.size() > 0 && excelDataTable.get(0).getRowChild().size() > 0) {
                rowChild.setHeight(ROW_HEIGHT);
            }
            for (int j = 0; j < excelDataTable.size(); j++) {
                excelDataTable.get(j).getRowChild().add(rowChild);
            }
        }
        invalidate();
        yExtendNum++;
    }

    /**
     * 取消一级扩展行
     */
    private void narrowHeight() {
        if (yExtendNum < 1) {
            return;
        }
        for (int i = 0; i < KUO_SIZE; i++) {
            for (int j = 0; j < excelDataTable.size(); j++) {
                excelDataTable.get(j).getRowChild().remove(excelDataTable.get(j).getRowChild().size() - 1);
            }
        }
        invalidate();
        yExtendNum--;
    }

    /**
     * 实时放大缩小比率
     *
     * @param zom 放大参数
     * @param fX  焦点 x
     * @param fY  焦点 y
     */
    private void zoomTable(float zom, float fX, float fY) {

        double wB = DoubleUtil.divide(excelDataTable.get(0).getWidth(), excelData.get(0).getWidth());
        //double hB = DoubleUtil.divide(excelDataTable.get(0).getRowChild().get(0).getHeight(), excelData.get(0).getRowChild().get(0).getHeight());

        Lag.i("正比: " + wB);
        Lag.i("焦点 fX:" + fX + " fY:" + fY);

        // 限制缩小放大范围
        if (wB < 0.9 && zom < 1 || wB > 4 && zom > 1) {
            return;
        }

        // 位置动态改变
        // 无焦点计算方式
        if (fX == -1 || fY == -1) {
            tableX = (float) DoubleUtil.multiply(tableX, zom);
            tableY = (float) DoubleUtil.multiply(tableY, zom);
            tableXMove = (float) DoubleUtil.multiply(tableXMove, zom);
            tableYMove = (float) DoubleUtil.multiply(tableYMove, zom);
        } else {
            float zFx = (float) DoubleUtil.multiply(fX, zom) - fX;
            float zFy = (float) DoubleUtil.multiply(fY, zom) - fY;
            tableX = (float) DoubleUtil.multiply(tableX, zom) - (float) zFx;
            tableY = (float) DoubleUtil.multiply(tableY, zom) - (float) zFy;
            // 避免越界
            if (tableX > 0) {
                tableX = 0;
            }
            if (tableY > 0) {
                tableY = 0;
            }
        }

        paintBorder.setStrokeWidth((float) DoubleUtil.multiply(DisplayUtil.dip2px(mContext, 1), zom));
        paintSelectBorder.setStrokeWidth((float) DoubleUtil.multiply(DisplayUtil.dip2px(mContext, 1), zom));
        paintExtendBorder.setStrokeWidth((float) DoubleUtil.multiply(DisplayUtil.dip2px(mContext, 1), zom));

        // 标准大小
        COL_WIDTH = (float) DoubleUtil.multiply(COL_WIDTH, zom);
        ROW_HEIGHT = (float) DoubleUtil.multiply(ROW_HEIGHT, zom);
        for (int i = 0; i < excelDataTable.size(); i++) {
            // 虚拟单元固定 避免异常
            if (i >= DATA_TABLE_COL_SIZE) {
                excelDataTable.get(i).setWidth(COL_WIDTH);
            } else {
                excelDataTable.get(i).setWidth(DoubleUtil.multiply(excelDataTable.get(i).getWidth(), zom));
            }
        }
        for (int j = 0; j < excelDataTable.get(0).getRowChild().size(); j++) {
            for (int i = 0; i < excelDataTable.size(); i++) {
                // 虚拟单元固定 避免异常
                if (j >= DATA_TABLE_ROW_SIZE) {
                    excelDataTable.get(i).getRowChild().get(j).setHeight(ROW_HEIGHT);
                } else {
                    excelDataTable.get(i).getRowChild().get(j).setHeight(
                            DoubleUtil.multiply(excelDataTable.get(i).getRowChild().get(j).getHeight(), zom));
                    excelDataTable.get(i).getRowChild().get(j).setTextSize(
                            (float) DoubleUtil.multiply(excelDataTable.get(i).getRowChild().get(j).getTextSize(), zom));
                }
            }
        }
        excelDataTable = reInitWidthHeight(excelDataTable);
        runTable();
        invalidate();
    }

    // ----------------------------------------------------------------------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        if (!hasInit) {
            initView(mContext);
        }
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // wrap_parent
        if (specMode == MeasureSpec.AT_MOST) {
            bigWidth = DisplayUtil.sp2px(mContext, 200);
        } else {
            bigWidth = specSize;
        }

        return (int) bigWidth;
    }

    private int measureHeight(int measureSpec) {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        // wrap_parent
        if (specMode == MeasureSpec.AT_MOST) {
            bigHeight = DisplayUtil.sp2px(mContext, 200);
        } else {
            bigHeight = specSize;
        }

        return (int) bigHeight;
    }

    // ----------------------------------------------------------------------------

    // 开启一次缩放动作
    private boolean isOpenScaleAction = false;

    /**
     * TODO: 出现抖动
     * 手势监听
     */
    private class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        //双手指操作
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            zoomTable(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            isOpenScaleAction = true;
            return true;
        }
    }

    /**
     * 根据位置反馈序号
     *
     * @return
     */
    private Point downYuan(float x, float y) {
        // 实际表格的位置
        float tx = (-tableX) + x;
        float ty = (-tableY) + y;
        float tWidthX = 0;
        float tWidthW = 0;
        for (int i = 0; i < excelDataTable.size(); i++) {
            float tHeightY = 0;
            float tHeightH = 0;
            tWidthW += excelDataTable.get(i).getWidth();
            for (int j = 0; j < excelDataTable.get(i).getRowChild().size(); j++) {
                // 换算当前单元格位置
                tHeightH += excelDataTable.get(i).getRowChild().get(j).getHeight();
                if (tx > tWidthX && tx < tWidthW && ty > tHeightY && ty < tHeightH) {
                    return new Point(i, j);
                }
                tHeightY += excelDataTable.get(i).getRowChild().get(j).getHeight();
            }
            tWidthX += excelDataTable.get(i).getWidth();
        }
        return new Point(-1, -1);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleGestureDetector.onTouchEvent(event);

        if (event.getPointerCount() > 1) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                stopSliding();

                tableDownTime = System.currentTimeMillis();

                tableXDown = event.getX();
                tableYDown = event.getY();

                nowSelectDown = downYuan(tableXDown, tableYDown);

                // 增加延迟互动条动画
                if (animEndTimer != null) {
                    animEndTimer.cancel();
                    animEndTimer = null;
                    if (animTimer != null) {
                        animTimer.cancel();
                        animTimer = null;
                    }
                } else {
                    startShowBar(10, true);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (animTimer == null) {
                    paintBar.setAlpha(255);
                    invalidate();
                }

                tableXMove = event.getX() - tableXDown;
                tableYMove = event.getY() - tableYDown;

                float leftX = tableX + tableXMove;
                float leftY = tableY + tableYMove;

                //Lag.i("leftX:" + leftX + " leftY:" + leftY);

                // 限制左上角
                if (leftX > 0) {
                    tableXMove = 0;
                    tableX = 0;
                    Lag.i("处于左边X边缘");
                }
                if (leftY > 0) {
                    tableYMove = 0;
                    tableY = 0;
                    Lag.i("处于顶部Y边缘");
                }

                // 只有在边缘放开扩展
                if (leftX < -(TABLE_WIDTH - bigWidth)) {
                    tableXMove = 0;
                    tableX = (float) -(TABLE_WIDTH - bigWidth);
                }
                if (leftY < -(TABLE_HEIGHT - bigHeight)) {
                    tableYMove = 0;
                    tableY = (float) -(TABLE_HEIGHT - bigHeight);
                }

                // 自动去除虚拟创建
                // 判断是否已创建，判断位置，判断方向
                if (xExtendNum > 1 && leftX < -(TABLE_WIDTH - bigWidth) + COL_WIDTH * KUO_SIZE && event.getX() - tableXDown > 0) {
                    Lag.i("处于虚拟扩展右边-1 X边缘");
                    narrowWidth();
                    excelDataTable = reInitWidthHeight(excelDataTable);
                    runTable();
                }
                if (yExtendNum > 1 && leftY < -(TABLE_HEIGHT - bigHeight) + ROW_HEIGHT * KUO_SIZE && event.getY() - tableYDown > 0) {
                    Lag.i("处于虚拟扩展底边-1 Y边缘");
                    narrowHeight();
                    excelDataTable = reInitWidthHeight(excelDataTable);
                    runTable();
                }

                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                // 如果开启了缩放动作，取消放开动作
                if (isOpenScaleAction) {
                    tableXMove = 0;
                    tableYMove = 0;
                    isOpenScaleAction = false;
                    return true;
                }

                // 小于500ms, 判断为滑动
                if (System.currentTimeMillis() - tableDownTime < 300) {
                    slidingToScreenXY(event.getX() - tableXDown, event.getY() - tableYDown,
                            System.currentTimeMillis() - tableDownTime, 0.3f);
                }

                nowSelectUp = downYuan(event.getX(), event.getY());

                tableXMove = 0;
                tableYMove = 0;
                tableX += event.getX() - tableXDown;
                tableY += event.getY() - tableYDown;
                if (tableX + tableXMove > 0) {
                    tableXMove = 0;
                    tableX = 0;
                }
                if (tableY + tableYMove > 0) {
                    tableYMove = 0;
                    tableY = 0;
                }
                float leftX2 = tableX + tableXMove;
                float leftY2 = tableY + tableYMove;

                if (leftX2 < -(TABLE_WIDTH - bigWidth)) {
                    tableXMove = 0;
                    tableX = (float) -(TABLE_WIDTH - bigWidth);
                    Lag.i("处于虚拟扩展右边X边缘");
                    extendWidth();
                    excelDataTable = reInitWidthHeight(excelDataTable);
                    runTable();
                }
                if (leftY2 < -(TABLE_HEIGHT - bigHeight)) {
                    tableYMove = 0;
                    tableY = (float) -(TABLE_HEIGHT - bigHeight);
                    Lag.i("处于虚拟扩展底边Y边缘");
                    extendHeight();
                    excelDataTable = reInitWidthHeight(excelDataTable);
                    runTable();
                }

                invalidate();

                // 延迟动画动作
                if (animEndTimer != null) {
                    animEndTimer.cancel();
                    animEndTimer = null;
                }
                if (animTimer != null) {
                    animTimer.cancel();
                    animTimer = null;
                }
                animEndTimer = new Timer();
                animEndTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        startShowBar(10, false);
                        animEndTimer.cancel();
                        animEndTimer = null;
                    }
                }, 2000);
                break;
            default:
                break;
        }
        return true;
    }
}
