package at.co.shaman.smartpv;

import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class LineChartListener implements OnChartGestureListener {

    private LineChart mLineChart;
    LineChartListener(LineChart lineChart) {
        mLineChart = lineChart;
    }
    /**
     * Callbacks when a touch-gesture has started on the chart (ACTION_DOWN)
     *
     * @param me
     * @param lastPerformedGesture
     */
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
    {
        checkScale();
    }

    /**
     * Callbacks when a touch-gesture has ended on the chart (ACTION_UP, ACTION_CANCEL)
     *
     * @param me
     * @param lastPerformedGesture
     */
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
    {
        checkScale();
    }

    /**
     * Callbacks when the chart is longpressed.
     *
     * @param me
     */
    public void onChartLongPressed(MotionEvent me)
    {
        checkScale();
    }

    /**
     * Callbacks when the chart is double-tapped.
     *
     * @param me
     */
    public void onChartDoubleTapped(MotionEvent me)
    {
        checkScale();
    }

    /**
     * Callbacks when the chart is single-tapped.
     *
     * @param me
     */
    public void onChartSingleTapped(MotionEvent me)
    {
        checkScale();
    }

    /**
     * Callbacks then a fling gesture is made on the chart.
     *
     * @param me1
     * @param me2
     * @param velocityX
     * @param velocityY
     */
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY)
    {
        checkScale();
    }

    /**
     * Callbacks when the chart is scaled / zoomed via pinch zoom gesture.
     *
     * @param me
     * @param scaleX scalefactor on the x-axis
     * @param scaleY scalefactor on the y-axis
     */
    public void onChartScale(MotionEvent me, float scaleX, float scaleY)
    {
        checkScale();
    }

    /**
     * Callbacks when the chart is moved / translated via drag gesture.
     *
     * @param me
     * @param dX translation distance on the x-axis
     * @param dY translation distance on the y-axis
     */
    public void onChartTranslate(MotionEvent me, float dX, float dY)
    {
        checkScale();
    }

    public void checkScale() {
        ViewPortHandler viewPortHandler = mLineChart.getViewPortHandler();
        float scaleX = viewPortHandler.getScaleX();
        float scaleY = viewPortHandler.getScaleY();

        XAxis xAxis = mLineChart.getXAxis();
        if( ( scaleX < 1.001f) && ( scaleY < 1.001f ) ) {
            xAxis.setShowSpecificLabelPositions(true);
            xAxis.setSpecificLabelPositions(new float[]{0, 3 * 3600000, 6 * 3600000, 9 * 3600000, 12 * 3600000, 15 * 3600000, 18 * 3600000, 21 * 3600000, 24 * 3600000});
        } else {
            xAxis.setShowSpecificLabelPositions(false);
        }
    }

}