package at.co.shaman.smartpv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.beardedhen.androidbootstrap.BootstrapProgressBar;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Double.max;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwipeRefreshLayout mSwipeRefreshBoiler;
    private RelativeLayout mLayoutCurrent;
    private RelativeLayout mLayoutHistory;
    private RelativeLayout mLayoutForecast;
    private RelativeLayout mLayoutBoiler;
    private TextView mLblCurrent;
    private TextView mLblHistory;
    private TextView mLblForecast;
    private TextView mLblBoiler;
    private TextView mLblHistoryDay;
    private TextView mLblHistoryWeek;
    private TextView mLblHistoryMonth;
    private TextView mLblHistoryYear;
    private TextView mLblHistoryTotal;
    private Double mTotalFactor;
    private String mHistoryMode;
    private int mIndexPlotDaily;
    private int mIndexPlotBar;
    private int mIndexWeatherDay;
    private String mPlotNameDaily[];
    private String mPlotNameBar[];
    StringBuilder mWeatherContent;
    TextView[] mArTimes;
    ImageView[] mArIcons;
    TextView[] mArTemps;
    TextView[] mArSky;
    TextView[] mArPop;
    ImageView[] mArWind;
    TextView[] mArWindSpeed;
    TextView[] mArPrediction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTotalFactor = 1.0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshBoiler = (SwipeRefreshLayout) findViewById(R.id.swipelayoutBoiler);
        mLayoutCurrent = findViewById(R.id.layoutCurrent);
        mLayoutHistory = findViewById(R.id.layoutHistory);
        mLayoutForecast = findViewById(R.id.layoutForecast);
        mLayoutBoiler = findViewById(R.id.layoutBoiler);
        mLblCurrent = findViewById(R.id.lblCurrent);
        mLblHistory = findViewById(R.id.lblHistory);
        mLblForecast = findViewById(R.id.lblForecast);
        mLblBoiler = findViewById(R.id.lblBoiler);
        mLblHistoryDay = findViewById(R.id.lblDay);
        mLblHistoryWeek = findViewById(R.id.lblWeek);
        mLblHistoryMonth = findViewById(R.id.lblMonth);
        mLblHistoryYear = findViewById(R.id.lblYear);
        mLblHistoryTotal = findViewById(R.id.lblTotal);
        mPlotNameDaily = new String[]{ "Energy", "Energy difference" };
        mPlotNameBar = new String[]{ "Energy", "Earning" };
        mIndexPlotDaily = 0;
        mIndexPlotBar = 0;
        mIndexWeatherDay = 0;
        initialiseWeatherArrays();
        BootstrapProgressBar bar = findViewById((R.id.barProductionNow));
        bar.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
        bar = findViewById((R.id.barConsumptionNow));
        bar.setBootstrapBrand(DefaultBootstrapBrand.WARNING);
        LineChart volumeReportChart = findViewById(R.id.reportingChart);
        setupPlotDay(volumeReportChart);
        LineChart boilerChart = findViewById(R.id.chartBoiler);
        setupPlotDay(boilerChart);
        ImageView img = findViewById(R.id.imageWeatherIconMain);
        img.setImageDrawable(null);

        TextView targetTime = findViewById(R.id.edtTargetTime);
        targetTime.setText( String.format( "%02d:%02d", 20, 0 ) );

        targetTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub

                EditText tm = findViewById( R.id.edtTargetTime );
                String str = tm.getText().toString();
                int force_target_hour = 0;
                int force_target_minute = 0;
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                try {
                    Date date = format.parse(str);
                    force_target_hour = date.getHours();
                    force_target_minute = date.getMinutes();
                } catch ( ParseException e ) {

                }

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        targetTime.setText( String.format( "%02d:%02d", selectedHour , selectedMinute ) );
                    }
                }, force_target_hour, force_target_minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        mSwipeRefreshBoiler.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContentBoiler();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        openCurrent();
        loadMainData();

        ImageView current = findViewById(R.id.imageCurrent);
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrent();
            }
        });

        mLblCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrent();
            }
        });

        ImageView history = findViewById(R.id.imageHistory);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });

        mLblHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });

        ImageView forecast = findViewById(R.id.imageForecast);
        forecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForecast();
            }
        });

        mLblForecast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForecast();
            }
        });

        ImageView boiler = findViewById(R.id.imageBoiler);
        boiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoiler();
            }
        });

        mLblBoiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBoiler();
            }
        });

        TextView prevDay = findViewById(R.id.btnHistoryPrev);
        prevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrev();
            }
        });

        TextView nextDay = findViewById(R.id.btnHistoryNext);
        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext();
            }
        });

        mLblHistoryDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryThisDay();
            }
        });

        mLblHistoryWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryThisWeek();
            }
        });

        mLblHistoryMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryThisMonth();
            }
        });

        mLblHistoryYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryThisYear();
            }
        });

        mLblHistoryTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistoryTotal();
            }
        });

        TextView lblWeatherPrev = findViewById(R.id.btnWeatherPrev);
        lblWeatherPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIndexWeatherDay -= 1;
                resetWeatherArrays();
                showWeather();
            }
        });

        TextView lblWeatherNext = findViewById(R.id.btnWeatherNext);
        lblWeatherNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIndexWeatherDay += 1;
                resetWeatherArrays();
                showWeather();
            }
        });

        TextView prevDayBoiler = findViewById(R.id.btnBoilerPrev);
        prevDayBoiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPrevBoiler();
                loadBoilerDay();
            }
        });

        TextView nextDayBoiler = findViewById(R.id.btnBoilerNext);
        nextDayBoiler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextBoiler();
                loadBoilerDay();
            }
        });

        TextView plotPrev = findViewById(R.id.btnPlotPrev);
        plotPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlotPrev();
            }
        });

        TextView plotNext = findViewById(R.id.btnPlotNext);
        plotNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlotNext();
            }
        });

        Button btn = findViewById(R.id.btnApplyBoiler);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyBoilerChanges();
            }
        });

        LineChart volumeReportChart = findViewById(R.id.reportingChart);
        volumeReportChart.setOnChartGestureListener(new LineChartListener( volumeReportChart ) );

        volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setOnChartGestureListener(new LineChartListener( volumeReportChart ) );

        volumeReportChart = findViewById(R.id.chartBoiler);
        volumeReportChart.setOnChartGestureListener(new LineChartListener( volumeReportChart ) );
    }

    public void onPlotPrev()
    {
        if( mHistoryMode == "day" ) {
            mIndexPlotDaily -= 1;
            if( mIndexPlotDaily < 0 ) {
                mIndexPlotDaily = mPlotNameDaily.length - 1;
            }
            drawHistoryDay();
            TextView lbl = findViewById( R.id.lblPlotName );
            lbl.setText( mPlotNameDaily[ mIndexPlotDaily ] );
        } else {
            mIndexPlotBar -= 1;
            if( mIndexPlotBar < 0 ) {
                mIndexPlotBar = mPlotNameBar.length - 1;
            }
            TextView lbl = findViewById( R.id.lblPlotName );
            lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
            if( mHistoryMode == "week" ) {
                drawHistoryWeek();
            } else if( mHistoryMode == "month" ) {
                drawHistoryMonth();
            } else if( mHistoryMode == "year" ) {
                drawHistoryYear();
            }
            else if( mHistoryMode == "total" ) {
                drawHistoryTotal();
            }
        }
    }

    public void onPlotNext()
    {
        if( mHistoryMode == "day" ) {
            mIndexPlotDaily += 1;
            if( mIndexPlotDaily >= mPlotNameDaily.length ) {
                mIndexPlotDaily = 0;
            }
            drawHistoryDay();
            TextView lbl = findViewById( R.id.lblPlotName );
            lbl.setText( mPlotNameDaily[ mIndexPlotDaily ] );
        } else {
            mIndexPlotBar += 1;
            if( mIndexPlotBar >= mPlotNameDaily.length ) {
                mIndexPlotBar = 0;
            }
            TextView lbl = findViewById( R.id.lblPlotName );
            lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
            if( mHistoryMode == "week" ) {
                drawHistoryWeek();
            } else if( mHistoryMode == "month" ) {
                drawHistoryMonth();
            } else if( mHistoryMode == "year" ) {
                drawHistoryYear();
            }
            else if( mHistoryMode == "total" ) {
                drawHistoryTotal();
            }
        }
    }

    public String formatStrDay(Integer diff)  {
        String str_out = "";
        try {
            TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
            String str = viewDay.getText().toString();

            Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, diff);
            date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            str_out = sdf.format(date);

            sdf = new SimpleDateFormat("dd MMMM yyyy" );

            String str_today = sdf.format(date);
            viewDay.setText( str_today );
        } catch( ParseException e ) {
            // oops
        }
        return str_out;
    }

    public String formatStrWeek(Integer diff)  {
        String str_out = "";
        try {
            TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
            String str = viewDay.getText().toString();
            str = str.substring(0,8);

            Date date = new SimpleDateFormat("dd.MM.yy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, diff * 7);
            Date dt = cal.getTime();
            Integer year = cal.get(Calendar.YEAR);
            Integer month = cal.get(Calendar.MONTH) + 1;
            Integer day = cal.get(Calendar.DAY_OF_MONTH);
            LocalDate date_local = LocalDate.of(year, month, day);
            Integer week = date_local.get(WeekFields.ISO.weekOfWeekBasedYear());
            date = cal.getTime();
            cal.add(Calendar.DATE, 6);
            Date date2 = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            str_out = sdf.format(date) + week.toString();

            sdf = new SimpleDateFormat("dd.MM.yy" );

            String str1 = sdf.format(date);
            String str2 = sdf.format(date2);
            viewDay.setText( str1 + " - " + str2 );
        } catch( ParseException e ) {
            // oops
        }
        return str_out;
    }

    public String formatStrMonth(Integer diff)  {
        String str_out = "";
        try {
            TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
            String str = viewDay.getText().toString();

            Date date = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, diff);
            date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            str_out = sdf.format(date);

            sdf = new SimpleDateFormat("MMMM yyyy" );

            String str_today = sdf.format(date);
            viewDay.setText( str_today );
        } catch( ParseException e ) {
            // oops
        }
        return str_out;
    }

    public String formatStrYear(Integer diff)  {
        String str_out = "";
        try {
            TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
            String str = viewDay.getText().toString();

            Date date = new SimpleDateFormat("yyyy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.YEAR, diff);
            date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            str_out = sdf.format(date);

            String str_today = sdf.format(date);
            viewDay.setText( str_today );
        } catch( ParseException e ) {
            // oops
        }
        return str_out;
    }

    public void onPrev()
    {
        if( mHistoryMode == "day" ) {
            String str = formatStrDay(-1);
            drawHistoryDay();
            TextView textNext = findViewById(R.id.btnHistoryNext);
            textNext.setVisibility(View.VISIBLE);
        } else if( mHistoryMode == "week" ) {
            String str = formatStrWeek( -1 );
            drawHistoryWeek();
            TextView textNext = findViewById(R.id.btnHistoryNext);
            textNext.setVisibility(View.VISIBLE);
        } else if( mHistoryMode == "month" ) {
            String str = formatStrMonth( -1 );
            drawHistoryMonth();
            TextView textNext = findViewById(R.id.btnHistoryNext);
            textNext.setVisibility(View.VISIBLE);
        }  else if( mHistoryMode == "year" ) {
            String str = formatStrYear( -1 );
            drawHistoryYear();
            TextView textNext = findViewById(R.id.btnHistoryNext);
            textNext.setVisibility(View.VISIBLE);
        }
    }

    public void onNext()
    {
        if( mHistoryMode == "day" ) {
            String str = formatStrDay(1);
            drawHistoryDay();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String str_today = sdf.format(c);
            if (str_today.equals(str)) {
                TextView textHist = findViewById(R.id.btnHistoryNext);
                textHist.setVisibility(View.INVISIBLE);
            }
        } else if( mHistoryMode == "week") {
            String str = formatStrWeek(1);
            drawHistoryWeek();
            Calendar calendar = Calendar.getInstance();
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
            Date d = calendar.getTime();
            String str_today = sdf.format(d);
            TextView view = findViewById(R.id.lblHistoryDaySelect);
            String str2 = view.getText().toString().substring(0,8);
            if (str_today.equals(str2)) {
                TextView textHist = findViewById(R.id.btnHistoryNext);
                textHist.setVisibility(View.INVISIBLE);
            }
        } else if( mHistoryMode == "month") {
            String str = formatStrMonth(45);
            drawHistoryMonth();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            String str_today = sdf.format(c);
            if (str_today.equals(str)) {
                TextView textHist = findViewById(R.id.btnHistoryNext);
                textHist.setVisibility(View.INVISIBLE);
            }

        } else if( mHistoryMode == "year") {
            String str = formatStrYear(1);
            drawHistoryYear();
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String str_today = sdf.format(c);
            if (str_today.equals(str)) {
                TextView textHist = findViewById(R.id.btnHistoryNext);
                textHist.setVisibility(View.INVISIBLE);
            }

        }

    }

    public void onPrevBoiler() {
        try {
            String str_out = "";
            TextView viewDay = findViewById(R.id.lblBoilerDaySelect);
            String str = viewDay.getText().toString();

            Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, -1);
            date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            str_out = sdf.format(date);

            viewDay.setText( str_out );
            TextView viewYesterday = findViewById(R.id.btnBoilerNext);
            viewYesterday.setVisibility(View.VISIBLE);
        } catch( ParseException e ) {
            // oops
        }
    }

    public void onNextBoiler() {
        try {
            String str_out = "";
            TextView viewDay = findViewById(R.id.lblBoilerDaySelect);
            String str = viewDay.getText().toString();

            Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).parse(str);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            str_out = sdf.format(date);

            viewDay.setText( str_out );

            Date c = Calendar.getInstance().getTime();
            String str_today = sdf.format(c);
            if (str_today.equals(str_out)) {
                TextView textNext = findViewById(R.id.btnBoilerNext);
                textNext.setVisibility(View.INVISIBLE);
            }
        } catch( ParseException e ) {
            // oops
        }

    }

    public void openCurrent() {
        mLblCurrent.setTextColor(0xffff0000);
        mLblHistory.setTextColor(0xff000000);
        mLblForecast.setTextColor(0xff000000);
        mLblBoiler.setTextColor(0xff000000);
        mLayoutCurrent.setVisibility(RelativeLayout.VISIBLE);
        mLayoutHistory.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutForecast.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutBoiler.setVisibility(RelativeLayout.INVISIBLE);
        mIndexPlotDaily = 0;


    }

    public void openHistory() {
        mLblCurrent.setTextColor(0xff000000);
        mLblHistory.setTextColor(0xffff0000);
        mLblForecast.setTextColor(0xff000000);
        mLblBoiler.setTextColor(0xff000000);
        mLayoutCurrent.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutHistory.setVisibility(RelativeLayout.VISIBLE);
        mLayoutForecast.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutBoiler.setVisibility(RelativeLayout.INVISIBLE);

       openHistoryThisDay();
    }

    public void openForecast() {
        mLblCurrent.setTextColor(0xff000000);
        mLblHistory.setTextColor(0xff000000);
        mLblForecast.setTextColor(0xffff0000);
        mLblBoiler.setTextColor(0xff000000);
        mLayoutCurrent.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutHistory.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutForecast.setVisibility(RelativeLayout.VISIBLE);
        mLayoutBoiler.setVisibility(RelativeLayout.INVISIBLE);
        resetWeatherArrays();

        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str_out = formatStrMonth(0);
                    mWeatherContent = getREST("http://nas8055.synology.me:50111/rest/weather_forecast");
                    showWeather();
                }
            });
        });
        mIndexWeatherDay = 0;
    }

    public void openBoiler() {
        mLblCurrent.setTextColor(0xff000000);
        mLblHistory.setTextColor(0xff000000);
        mLblForecast.setTextColor(0xff000000);
        mLblBoiler.setTextColor(0xffff0000);
        mLayoutCurrent.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutHistory.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutForecast.setVisibility(RelativeLayout.INVISIBLE);
        mLayoutBoiler.setVisibility(RelativeLayout.VISIBLE);

        TextView viewNext = findViewById(R.id.btnBoilerNext);
        viewNext.setVisibility(View.INVISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy" );
        Date c = Calendar.getInstance().getTime();
        String str_today = sdf.format(c);
        TextView viewDay = findViewById(R.id.lblBoilerDaySelect);
        viewDay.setText( str_today );

        loadBoilerData();
        loadBoilerDay();
    }

    public void openHistoryThisDay() {
        mHistoryMode = "day";
        mLblHistoryDay.setTextColor(0xffff0000);
        mLblHistoryWeek.setTextColor(0xff000000);
        mLblHistoryMonth.setTextColor(0xff000000);
        mLblHistoryYear.setTextColor(0xff000000);
        mLblHistoryTotal.setTextColor(0xff000000);
        showAverages(false);

        LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setVisibility(View.VISIBLE);
        BarChart volumeReportBar = findViewById(R.id.reportingChartHistoryBar);
        volumeReportBar.setVisibility(View.INVISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy" );
        Date c = Calendar.getInstance().getTime();
        String str_today = sdf.format(c);
        TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
        viewDay.setText( str_today );

        TextView lblNextDay = findViewById(R.id.btnHistoryNext);
        lblNextDay.setVisibility(View.INVISIBLE);
        TextView lblPrevDay = findViewById(R.id.btnHistoryPrev);
        lblPrevDay.setVisibility(View.VISIBLE);
        drawHistoryDay();

    }

    public void openHistoryThisWeek() {
        mHistoryMode = "week";
        mLblHistoryDay.setTextColor(0xff000000);
        mLblHistoryWeek.setTextColor(0xffff0000);
        mLblHistoryMonth.setTextColor(0xff000000);
        mLblHistoryYear.setTextColor(0xff000000);
        mLblHistoryTotal.setTextColor(0xff000000);
        showAverages(true);

        LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setVisibility(View.INVISIBLE);
        BarChart volumeReportBar = findViewById(R.id.reportingChartHistoryBar);
        volumeReportBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy" );

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        String day1 = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        String day2 = sdf.format(calendar.getTime());

        TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
        viewDay.setText( day1 + " - " + day2 );

        TextView lblNextDay = findViewById(R.id.btnHistoryNext);
        lblNextDay.setVisibility(View.INVISIBLE);
        TextView lblPrevDay = findViewById(R.id.btnHistoryPrev);
        lblPrevDay.setVisibility(View.VISIBLE);
        drawHistoryWeek();
    }

    public void openHistoryThisMonth() {
        mHistoryMode = "month";
        mLblHistoryDay.setTextColor(0xff000000);
        mLblHistoryWeek.setTextColor(0xff000000);
        mLblHistoryMonth.setTextColor(0xffff0000);
        mLblHistoryYear.setTextColor(0xff000000);
        mLblHistoryTotal.setTextColor(0xff000000);
        showAverages(true);

        LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setVisibility(View.INVISIBLE);
        BarChart volumeReportBar = findViewById(R.id.reportingChartHistoryBar);
        volumeReportBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy" );
        Date c = Calendar.getInstance().getTime();
        String str_today = sdf.format(c);
        TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
        viewDay.setText( str_today );

        TextView lblNextDay = findViewById(R.id.btnHistoryNext);
        lblNextDay.setVisibility(View.INVISIBLE);
        TextView lblPrevDay = findViewById(R.id.btnHistoryPrev);
        lblPrevDay.setVisibility(View.VISIBLE);
        drawHistoryMonth();
    }

    public void openHistoryThisYear() {
        mHistoryMode = "year";
        mLblHistoryDay.setTextColor(0xff000000);
        mLblHistoryWeek.setTextColor(0xff000000);
        mLblHistoryMonth.setTextColor(0xff000000);
        mLblHistoryYear.setTextColor(0xffff0000);
        mLblHistoryTotal.setTextColor(0xff000000);
        showAverages(false);

        LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setVisibility(View.INVISIBLE);
        BarChart volumeReportBar = findViewById(R.id.reportingChartHistoryBar);
        volumeReportBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" );
        Date c = Calendar.getInstance().getTime();
        String str_today = sdf.format(c);
        TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
        viewDay.setText( str_today );

        TextView lblNextDay = findViewById(R.id.btnHistoryNext);
        lblNextDay.setVisibility(View.INVISIBLE);
        TextView lblPrevDay = findViewById(R.id.btnHistoryPrev);
        lblPrevDay.setVisibility(View.VISIBLE);
        drawHistoryYear();
    }

    public void openHistoryTotal() {
        mHistoryMode = "total";
        mLblHistoryDay.setTextColor(0xff000000);
        mLblHistoryWeek.setTextColor(0xff000000);
        mLblHistoryMonth.setTextColor(0xff000000);
        mLblHistoryYear.setTextColor(0xff000000);
        mLblHistoryTotal.setTextColor(0xffff0000);
        showAverages(false);

        LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
        volumeReportChart.setVisibility(View.INVISIBLE);
        BarChart volumeReportBar = findViewById(R.id.reportingChartHistoryBar);
        volumeReportBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" );
        Date c = Calendar.getInstance().getTime();
        String str_today = sdf.format(c);
        TextView viewDay = findViewById(R.id.lblHistoryDaySelect);
        viewDay.setText( "Total statistic" );

        TextView lblNextDay = findViewById(R.id.btnHistoryNext);
        lblNextDay.setVisibility(View.INVISIBLE);
        TextView lblPrevDay = findViewById(R.id.btnHistoryPrev);
        lblPrevDay.setVisibility(View.INVISIBLE);
        drawHistoryTotal();
    }

    public void drawHistoryDay()
    {
        TextView lbl = findViewById(R.id.lblPlotName);
        lbl.setText( mPlotNameDaily[ mIndexPlotDaily ] );
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str_out = formatStrDay(0);
                    StringBuilder content_day = getREST("http://nas8055.synology.me:50111/pv/day/" + str_out);
                    drawHistoryStatistic(content_day);
                    LineChart volumeReportChart = findViewById(R.id.reportingChartHistoryDay);
                    setupPlotDay(volumeReportChart);
                    drawDailyPlot(volumeReportChart, content_day);
                }
            });
        });
    }

    public void drawHistoryWeek()
    {
        TextView lbl = findViewById(R.id.lblPlotName);
        lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();

        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str_out = formatStrWeek(0);
                    StringBuilder content_week = getREST("http://nas8055.synology.me:50111/pv/week/" + str_out);
                    drawHistoryStatistic(content_week);
                    BarChart volumeReportChart = findViewById(R.id.reportingChartHistoryBar);
                    setupPlotBar(volumeReportChart,7);
                    int num_values = drawBarPlot(volumeReportChart, content_week, 1, 7);
                    showAvgStatistic( num_values );
                }
            });
        });
    }

    public void drawHistoryMonth()
    {
        TextView lbl = findViewById(R.id.lblPlotName);
        lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str_out = formatStrMonth(0);
                    StringBuilder content_month = getREST("http://nas8055.synology.me:50111/pv/month/" + str_out);
                    drawHistoryStatistic(content_month);
                    BarChart volumeReportChart = findViewById(R.id.reportingChartHistoryBar);

                    Integer year = Integer.valueOf( str_out.substring( 0, 4 ) );
                    Integer month = Integer.valueOf( str_out.substring( 4, 6 ) );
                    YearMonth yearMonthObject = YearMonth.of(year, month);
                    int daysInMonth = yearMonthObject.lengthOfMonth();

                    setupPlotBar(volumeReportChart,daysInMonth);
                    int num_values = drawBarPlot(volumeReportChart, content_month, 1, daysInMonth);
                    showAvgStatistic( num_values );
                }
            });
        });
    }

    public void drawHistoryYear()
    {
        TextView lbl = findViewById(R.id.lblPlotName);
        lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str_out = formatStrYear(0);
                    StringBuilder content_year = getREST("http://nas8055.synology.me:50111/pv/year/" + str_out);
                    drawHistoryStatistic(content_year);
                    BarChart volumeReportChart = findViewById(R.id.reportingChartHistoryBar);

                    setupPlotBar(volumeReportChart,12);
                    drawBarPlot(volumeReportChart, content_year, 1, 12);
                }
            });
        });
    }

    public void drawHistoryTotal()
    {
        TextView lbl = findViewById(R.id.lblPlotName);
        lbl.setText( mPlotNameBar[ mIndexPlotBar ] );
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        app.getExecutors().networkIO().execute(() -> {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int start_year = 2020;  // we have data starting from this year, I kknow it
                    StringBuilder content_total = getREST("http://nas8055.synology.me:50111/pv/total");
                    drawHistoryStatistic(content_total);
                    BarChart volumeReportChart = findViewById(R.id.reportingChartHistoryBar);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy" );
                    Date c = Calendar.getInstance().getTime();
                    String str_today = sdf.format(c);
                    Integer this_year = Integer.parseInt(str_today);

                    setupPlotBar(volumeReportChart,this_year - start_year + 1);
                    drawBarPlot(volumeReportChart, content_total, start_year, this_year);
                }
            });
        });
    }

    public void showWeather()
    {
        try {
            Resources res = getResources();

            JSONObject jWhole = new JSONObject((mWeatherContent.toString()));
            JSONObject jObject = jWhole.getJSONObject("weather_now");

            // show today
            Double temp = jObject.getDouble("temp");
            TextView lbl = findViewById(R.id.lblTemperature);
            int temp_int = Math.round( temp.floatValue() );
            String str = String.format( "%d\u00B0", temp_int );
            lbl.setText( str );

            String desc = jObject.getString("description" );
            Integer humidity = jObject.getInt( "humidity" );
            str = String.format( "%s, %d%%", desc, humidity );
            lbl = findViewById(R.id.lblTemperatureAdd);
            lbl.setText( str );

            temp = jObject.getDouble("feels_like");
            lbl = findViewById(R.id.lblFeelsLike);
            temp_int = Math.round( temp.floatValue() );
            str = String.format( "%d\u00B0", temp_int );
            lbl.setText( str );

            Double wind = jObject.getDouble("wind_speed");
            lbl = findViewById(R.id.lblWind);
            str = String.format( "%.1f m/s", wind );
            lbl.setText( str );

            Integer wind_dir = jObject.getInt( "wind_dir" );
            String str_wind = "arrow_" + getWindIcon( wind_dir );
            int resID = res.getIdentifier(str_wind , "drawable", getPackageName());
            ImageView imgWind = findViewById(R.id.imageWindDir);
            imgWind.setImageResource(resID);

            Double cloudiness = jObject.getDouble("cloudiness");
            lbl = findViewById(R.id.lblCloudiness);
            Integer cloudiness_int = Math.round( cloudiness.floatValue() );
            str = String.format( "%d%%", cloudiness_int );
            lbl.setText( str );

            String sunrise = jObject.getString("sunrise" );
            lbl = findViewById(R.id.lblSunrise);
            lbl.setText( sunrise );

            String sunset = jObject.getString("sunset" );
            lbl = findViewById(R.id.lblSunset);
            lbl.setText( sunset );

            String str_icon = "weather_" + jObject.getString("icon");
            resID = res.getIdentifier(str_icon , "drawable", getPackageName());
            ImageView imgIcon = findViewById(R.id.imageWeatherIcon);
            imgIcon.setImageResource(resID);

            // show forecast
            int vis = ImageView.VISIBLE;
            if( mIndexWeatherDay == 0 ) {
                vis = ImageView.INVISIBLE;
            }
            lbl = findViewById(R.id.btnWeatherPrev);
            lbl.setVisibility(vis);
            vis = ImageView.VISIBLE;
            if( mIndexWeatherDay == 3 ) {
                vis = ImageView.INVISIBLE;
            }
            lbl = findViewById(R.id.btnWeatherNext);
            lbl.setVisibility(vis);

            JSONObject jForecastAll = jWhole.getJSONObject("forecast");
            Date date = Calendar.getInstance().getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, mIndexWeatherDay);
            date = cal.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String formattedDate = df.format(date);
            JSONObject jForecast = jForecastAll.getJSONObject( formattedDate );
            String name = jForecast.getString("name");
            lbl = findViewById(R.id.lblWeatherDaySelect);
            lbl.setText( name);

            JSONArray jData = jForecast.getJSONArray("data");
            for( int i = 0; i < jData.length(); i++ ) {
                JSONObject jNext = jData.getJSONObject(i);
                String str_hour = jNext.getString( "hour" );
                int hour = Integer.parseInt( str_hour.substring(0,2) );
                if( ( hour < 6 ) || ( hour > 21 ) ) {
                    continue;
                }
                int idx = ( hour - 6 ) / 3;
                mArTimes[idx].setText(str_hour);

                str_icon = "weather_" + jNext.getString("icon");
                resID = res.getIdentifier(str_icon , "drawable", getPackageName());
                mArIcons[idx].setImageResource(resID);

                temp = jNext.getDouble("temp");
                temp_int = Math.round( temp.floatValue() );
                str = String.format( "%d\u00B0", temp_int );
                mArTemps[idx].setText( str );

                String str_sky = jNext.getString("sky");
                if( str_sky.length() > 7 ) {
                    str_sky = str_sky.substring(0,7);
                }
                mArSky[idx].setText(str_sky.toLowerCase());

                Double pop = jNext.getDouble("pop");
                Integer pop_int = Math.round( pop.floatValue() * 100.0f );
                str = String.format( "%d%%", pop_int );
                mArPop[idx].setText( str );

                wind_dir = jNext.getInt( "wind_dir" );
                str_wind = "arrow_" + getWindIcon( wind_dir );
                resID = res.getIdentifier(str_wind , "drawable", getPackageName());
                mArWind[idx].setImageResource(resID);

                wind = jNext.getDouble("wind_speed");
                str = String.format( "%.1f m/s", wind );
                mArWindSpeed[idx].setText( str );

                // TODO: set predictions
                mArPrediction[idx].setText("0 kWh");
            }

        } catch( JSONException e ) {
            // oops
        }
    }

    public void drawHistoryStatistic(StringBuilder content_day)
    {
        try {
            JSONArray jArray = new JSONArray((content_day.toString()));
            JSONObject jObject = jArray.getJSONObject(0);
            Double total = jObject.getDouble("e_total") * 0.001 * mTotalFactor;
            Double produce = jObject.getDouble("produce") * 0.001;
            Double consume = jObject.getDouble("consume") * 0.001;
            Double battery = jObject.getDouble("battery_proc") * 0.01 * 0.001 * 5120.0;
            TextView lbl2;
            lbl2 = findViewById(R.id.lblHistoryProduction);
            String str;
            str = String.format("%.2f kWh", total );
            lbl2.setText( str );
            lbl2 = findViewById(R.id.lblHistoryConsumption);
            Double consume_total = consume + ( total - produce );
            str = String.format("%.2f kWh", consume_total );
            lbl2.setText( str );
            lbl2 = findViewById(R.id.lblHistorySelfConsumption);
            Double self_consume = total - produce;
            str = String.format("%.2f kWh", self_consume );
            lbl2.setText( str );

            lbl2 = findViewById(R.id.lblHistoryFromGrid);
            str = String.format("%.2f kWh", consume );
            lbl2.setText( str );

            lbl2 = findViewById(R.id.lblHistoryFromBattery);
            str = String.format("%.2f kWh", battery );
            lbl2.setText( str );

            lbl2 = findViewById(R.id.lblHistorySelfConsumptionProc);
            Double self_consume_proc = 0.0;
            if( total > 0.001 ) {
                self_consume_proc = self_consume / total;
            }
            str = String.format("%d%%", Math.round( self_consume_proc * 100 ) );
            lbl2.setText( str );

            lbl2 = findViewById(R.id.lblHistorySelfGenerationProc);
            Double self_generate_proc = 0.0;
            if( consume_total > 0.001 ) {
                self_generate_proc = self_consume / consume_total;
            }
            str = String.format("%d%%", Math.round( self_generate_proc * 100 ) );
            lbl2.setText( str );

            if( mHistoryMode == "day" ) {
                Double tarif_consume = jObject.getDouble("tarif_consume");
                Double tarif_produce = jObject.getDouble("tarif_produce");
                lbl2 = findViewById(R.id.lblHistoryMoneySold);
                Double money_produce = produce * tarif_produce;
                str = String.format("%.2f EUR", money_produce);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneySaved);
                Double money_consume = (total - produce) * tarif_consume;
                str = String.format("%.2f EUR", money_consume);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneyTotal);
                str = String.format("%.2f EUR", money_consume + money_produce);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneySpent);
                str = String.format("%.2f EUR", consume * tarif_consume);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryBatteryEarn1);
                str = String.format("%.2f EUR", battery * tarif_consume);
                lbl2.setText(str);
            } else {
                Double earn_consume = jObject.getDouble("earn_consume");
                Double earn_produce = jObject.getDouble("earn_produce");
                Double earn_battery = jObject.getDouble("earn_battery");
                Double money_spent = jObject.getDouble("money_spent");
                lbl2 = findViewById(R.id.lblHistoryMoneySold);
                str = String.format("%.2f EUR", earn_produce);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneySaved);
                str = String.format("%.2f EUR", earn_consume);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneyTotal);
                str = String.format("%.2f EUR", earn_consume + earn_produce);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryMoneySpent);
                str = String.format("%.2f EUR", money_spent);
                lbl2.setText(str);
                lbl2 = findViewById(R.id.lblHistoryBatteryEarn1);
                str = String.format("%.2f EUR", earn_battery);
                lbl2.setText(str);
            }
        }
        catch( JSONException e ) {
            // oops
        }
    }

    private void refreshContent() {
        runOnUiThread(new Runnable() {
            public void run() {
                loadMainData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshContentBoiler() {
        runOnUiThread(new Runnable() {
            public void run() {
                loadBoilerData();
                loadBoilerDay();
                mSwipeRefreshBoiler.setRefreshing(false);
            }
        });
    }

    private void loadMainData() {
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();

        app.getExecutors().networkIO().execute(() -> {
            StringBuilder content_pv = getREST( "http://nas8055.synology.me:50111/pv/current" );
            StringBuilder content_today = getREST( "http://nas8055.synology.me:50111/pv/today" );

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        // Process "current"
                        JSONArray jArray = new JSONArray(( content_pv.toString() ));
                        JSONObject jObject = jArray.getJSONObject(0);
                        Double p = jObject.getDouble("p") * 0.001;
                        Double p_load = jObject.getDouble("p_load") * 0.001;

                        TextView lbl;
                        lbl = findViewById(R.id.lblProductionNow);
                        String str_p = String.format( "%.2f kWh", p );
                        lbl.setText( str_p );
                        TextView lbl2;
                        lbl2 = findViewById(R.id.lblConsumptionNow);
                        String str_p_load = String.format( "%.2f kWh", p_load );
                        lbl2.setText( str_p_load );
                        if( p > p_load )
                        {
                            lbl.setTextColor(0xff00a000);
                            lbl2.setTextColor(0xff00a000);
                        } else {
                            lbl.setTextColor(0xffff0000);
                            lbl2.setTextColor(0xffff0000);
                        }
                        Resources res = getResources();

                        BootstrapProgressBar bar = findViewById((R.id.barProductionNow));
                        Double progress = 100 * p / 8.5;
                        bar.setProgress( progress.intValue() );

                        bar = findViewById((R.id.barConsumptionNow));
                        progress = 100 * p_load / 8.5;
                        bar.setProgress( progress.intValue() );

                        Double batt_proc = jObject.getDouble("battery_proc");
                        Double batt_abs = batt_proc * 5.120 * 0.01; // capacity of our battery is 5120 kW
                        Double batt_dir = jObject.getDouble("battery_kWh");
                        String str_batt = String.format( "%.1f%% | %.2f kW | %.2f kWh", batt_proc, batt_abs, abs( batt_dir ) );
                        lbl = findViewById( R.id.lblBatteryText );
                        lbl.setText( str_batt);
                        ImageView imgBattery = findViewById(R.id.imageBattery);
                        ImageView imgBatteryDir = findViewById(R.id.imageBatteryDirection);
                        //imgBattery.setImageResource(resID);
                        int resDir = 0;
                        if( abs( batt_dir ) < 0.03 ) {
                            lbl.setTextColor(0xff000000);
                            resDir = res.getIdentifier("arrow_none" , "drawable", getPackageName());
                        } else if( batt_dir > 0 ) {
                            lbl.setTextColor(0xff00a000);
                            resDir = res.getIdentifier("arrow_up" , "drawable", getPackageName());
                        }  else if( batt_dir < 0 ) {
                            lbl.setTextColor(0xffc5c500);
                            resDir = res.getIdentifier("arrow_down" , "drawable", getPackageName());
                        }
                        imgBatteryDir.setImageResource(resDir);

                        int resBatt = 0;
                        if( batt_proc < 25 ) {
                            resBatt = res.getIdentifier("batt_0" , "drawable", getPackageName());
                        } else if ( batt_proc < 50 ) {
                            resBatt = res.getIdentifier("batt_25" , "drawable", getPackageName());
                        } else if( batt_proc < 75 ) {
                            resBatt = res.getIdentifier("batt_50" , "drawable", getPackageName());
                        } else {
                            resBatt = res.getIdentifier("batt_75" , "drawable", getPackageName());
                        }
                        imgBattery.setImageResource(resBatt);

                        // Process "today"
                        Double total_today = jObject.getDouble("e_total") * 0.001 * mTotalFactor;
                        Double consume_today = jObject.getDouble("consume") * 0.001;
                        Double produce_today = jObject.getDouble("produce") * 0.001;

                        lbl = findViewById(R.id.lblProductionToday);
                        String str = String.format("%.2f kWh", total_today );
                        lbl.setText( str );

                        lbl2 = findViewById(R.id.lblConsumptionToday);
                        Double consume_total = consume_today + ( total_today - produce_today );
                        str = String.format("%.2f kWh", consume_total );
                        lbl2.setText( str );

                        TextView lbl3 = findViewById(R.id.lblSelfConsumptionToday);
                        Double self_consume = total_today - produce_today;
                        str = String.format("%.2f kWh", self_consume );
                        lbl3.setText( str );

                        if( total_today > consume_today )
                        {
                            lbl.setTextColor(0xff00a000);
                            lbl2.setTextColor(0xff00a000);
                        } else {
                            lbl.setTextColor(0xffff0000);
                            lbl2.setTextColor(0xffff0000);
                        }
                        lbl3.setTextColor(0xff00a000);

                        // process today percentages
                        lbl = findViewById(R.id.lblProductionRate);
                        Double prod_rate = ( 100.0 * self_consume / total_today );
                        Integer prod_rate_int = Math.round( prod_rate.floatValue() );
                        if( prod_rate_int > 100 )
                        {
                            prod_rate_int = 100;
                        }
                        lbl.setText( prod_rate_int.toString() + "%" );
                        ProgressBar bar2 = findViewById(R.id.barProductionRate);
                        bar2.setProgress(prod_rate_int);

                        lbl = findViewById(R.id.lblConsumptionRate);
                        Double cons_rate = ( 100.0 * self_consume / consume_total );
                        Integer cons_rate_int = Math.round(cons_rate.floatValue());
                        if( cons_rate_int > 100 )
                        {
                            cons_rate_int = 100;
                        }
                        lbl.setText( cons_rate_int.toString() + "%" );
                        bar2 = findViewById(R.id.barConsumptionRate);
                        bar2.setProgress(cons_rate_int);

                        // earnings
                        Double tarif_prod = jObject.getDouble("tarif_produce");
                        Double tarif_cons = jObject.getDouble("tarif_consume");
                        Double earn_prod = tarif_prod * ( total_today - self_consume );
                        Double earn_cons = tarif_cons * self_consume;
                        Double earn = earn_prod + earn_cons;
                        lbl = findViewById(R.id.lblEarning);
                        str = String.format( "%.2f EUR", earn );
                        lbl.setText( str );

                        LineChart volumeToday = findViewById(R.id.reportingChart);
                        drawDailyPlot( volumeToday, content_today );

                        // process yesterday
                        Double total_y = jObject.getDouble("e_total_yesterday") * 0.001 * mTotalFactor;
                        Double consume_y = jObject.getDouble("consume_yesterday") * 0.001;
                        Double produce_y = jObject.getDouble("produce_yesterday") * 0.001;
                        Double self_consume_y = total_y - produce_y;
                        consume_y += self_consume_y;

                        lbl = findViewById(R.id.lblProductionYesterday);
                        str = String.format("%.2f kWh", total_y );
                        lbl.setText( str );

                        lbl2 = findViewById(R.id.lblConsumptionYesterday);
                        str = String.format("%.2f kWh", consume_y );
                        lbl2.setText( str );

                        lbl3 = findViewById(R.id.lblSelfConsumptionYesterday);
                        str = String.format("%.2f kWh", self_consume_y );
                        lbl3.setText( str );

                        if( total_y > consume_y )
                        {
                            lbl.setTextColor(0xff00a000);
                            lbl2.setTextColor(0xff00a000);
                        } else {
                            lbl.setTextColor(0xffff0000);
                            lbl2.setTextColor(0xffff0000);
                        }
                        lbl3.setTextColor(0xff00a000);

                        // process today percentages
                        lbl = findViewById(R.id.lblProductionRateYesterday);
                        Double prod_rate_y = ( 100.0 * self_consume_y / total_y );
                        Integer prod_rate_y_int = prod_rate_y.intValue();
                        if( prod_rate_y_int > 100 )
                        {
                            prod_rate_y_int = 100;
                        }
                        lbl.setText( prod_rate_y_int.toString() + "%" );
                        bar2 = findViewById(R.id.barProductionRateYesterday);
                        bar2.setProgress(prod_rate_y_int);

                        lbl = findViewById(R.id.lblConsumptionRateYesterday);
                        Double cons_rate_y = ( 100.0 * self_consume_y / consume_y );
                        Integer cons_rate_y_int = cons_rate_y.intValue();
                        if( cons_rate_y_int > 100 )
                        {
                            cons_rate_y_int = 100;
                        }
                        lbl.setText( cons_rate_y_int.toString() + "%" );
                        bar2 = findViewById(R.id.barConsumptionRateYesterday);
                        bar2.setProgress(cons_rate_y_int);

                        // earnings
                        Double earn_prod_y = tarif_prod * ( total_y - self_consume_y );
                        Double earn_cons_y = tarif_cons * self_consume_y;
                        Double earn_y = earn_prod_y + earn_cons_y;
                        lbl = findViewById(R.id.lblEarningYesterday);
                        str = String.format( "%.2f EUR", earn_y );
                        lbl.setText( str );

                        // weather
                        JSONObject jWeather = jObject.getJSONObject("weather_now");
                        String str_icon = "weather_" + jWeather.getString("icon");
                        int resID = res.getIdentifier(str_icon , "drawable", getPackageName());
                        ImageView imgIcon = findViewById(R.id.imageWeatherIconMain);
                        imgIcon.setImageResource(resID);

                        Double temp = jWeather.getDouble("temp");
                        lbl = findViewById(R.id.lblTemperatureMain);
                        int temp_int = Math.round( temp.floatValue() );
                        str = String.format( "%d\u00B0", temp_int );
                        lbl.setText( str );


                    } catch (JSONException e) {
                        // Oops
                    }
                }
            });
        });
    }

    private void loadBoilerData() {
        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();

        app.getExecutors().networkIO().execute(() -> {
            StringBuilder content_general = getREST( "http://nas8055.synology.me:50111/rest/general" );
            StringBuilder content_pv = getREST( "http://nas8055.synology.me:50111/pv/current" );
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        // Process "current"
                        JSONArray jArray = new JSONArray(( content_pv.toString() ));
                        JSONObject jObject = jArray.getJSONObject(0);
                        Double p = jObject.getDouble("p") * 0.001;
                        Double p_load = jObject.getDouble("p_load") * 0.001;
                        TextView lbl1;
                        lbl1 = findViewById(R.id.lblCurrentProduction);
                        String str_p = String.format( "%.2f kWh", p );
                        lbl1.setText( str_p );
                        TextView lbl2;
                        lbl2 = findViewById(R.id.lblCurrentConsumption2);
                        String str_p_load = String.format( "%.2f kWh", p_load );
                        lbl2.setText( str_p_load );
                        if( p > p_load )
                        {
                            lbl1.setTextColor(0xff00a000);
                            lbl2.setTextColor(0xff00a000);
                        } else {
                            lbl1.setTextColor(0xffff0000);
                            lbl2.setTextColor(0xffff0000);
                        }

                        // Process "boiler"
                        JSONObject jGeneral = new JSONObject(( content_general.toString() ));
                        JSONObject jBoiler = jGeneral.getJSONObject("Boiler");
                        String energy_state = jBoiler.getString("energy_state");
                        String temp = jBoiler.getString("temp");
                        Integer min_temp = jBoiler.getInt( "min_temperature" );
                        Integer max_temp = jBoiler.getInt( "max_temperature" );
                        Integer min_batt = jBoiler.getInt( "min_battery" );
                        Integer max_batt = jBoiler.getInt( "max_battery" );
                        Integer force_target_temp = jBoiler.getInt( "force_target_temperature" );
                        Integer force_target_hour = jBoiler.getInt( "force_target_time_hour" );
                        Integer force_target_minute = jBoiler.getInt( "force_target_time_minute" );
                        Integer smart_mode = jBoiler.getInt( "smart_mode" );
                        Integer force_mode = jBoiler.getInt( "force_mode" );
                        String reason = jBoiler.getString( "reason" );

                        TextView lbl = findViewById(R.id.numMinTemperature);
                        lbl.setText( min_temp.toString() );
                        lbl = findViewById(R.id.numMaxTemperature);
                        lbl.setText( max_temp.toString() );

                        lbl = findViewById(R.id.numMinBattery);
                        lbl.setText( min_batt.toString() );
                        lbl = findViewById(R.id.numMaxBattery);
                        lbl.setText( max_batt.toString() );

                        lbl = findViewById(R.id.lblCurrentTemperature);
                        lbl.setText( temp );
                        double d_temp = Double.parseDouble(temp );
                        if( d_temp > 42.5 )
                        {
                            lbl.setTextColor(0xff00a000);
                        } else {
                            lbl.setTextColor(0xffff0000);
                        }
                        EditText tm = findViewById( R.id.edtTargetTime );
                        tm.setText( String.format( "%02d:%02d", force_target_hour , force_target_minute ) );

                        Switch btn = findViewById(R.id.switchSmartMode);
                        btn.setChecked( (smart_mode > 0) );
                        btn = findViewById(R.id.switchForceMode);
                        btn.setChecked( force_mode > 0 );
                        btn = findViewById(R.id.switchBoilerState);
                        lbl = findViewById(R.id.lblBoilerReason);
                        if( energy_state.equals( "ON" ) ) {
                            btn.setChecked(true);
                            lbl.setText("(" + reason + ")" );
                            btn.setEnabled(true);
                        } else {
                            btn.setChecked(false);
                            lbl.setText("");
                            btn.setEnabled(false);
                        }
                    } catch (JSONException e) {
                        // Oops
                    }
                }
            });
        });
    }

    private void loadBoilerDay() {
        String str_out = "";
        try {
            TextView day = findViewById(R.id.lblBoilerDaySelect);
            Date date = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).parse(day.getText().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            str_out = sdf.format(date);

        } catch( ParseException e ) {
            // oops
        }

        final SmartPV app = (SmartPV)getApplication();
        Context context = getApplicationContext();
        final String str_out2 = str_out;

        app.getExecutors().networkIO().execute(() -> {
            StringBuilder content_info = getREST( "http://nas8055.synology.me:50111/rest/boiler_energy_info/" + str_out2 );
            StringBuilder content_plot = getREST( "http://nas8055.synology.me:50111/rest/boiler_day/" + str_out2 );

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        // process boiler info
                        JSONObject jInfo = new JSONObject(( content_info.toString() ));
                        Double boiler_energy = jInfo.getDouble("total_boiler_energy");
                        Double consume_energy = jInfo.getDouble("total_consume");
                        Integer total_minutes = jInfo.getInt("total_minutes");
                        Integer hours = total_minutes / 60;
                        Integer minutes = total_minutes % 60;
                        TextView lblInfo = findViewById(R.id.lblCurrentBoilerInfo);
                        float proc_float = ( ( boiler_energy.floatValue() - consume_energy.floatValue() ) / boiler_energy.floatValue() ) * 100;
                        if( boiler_energy < 0.01 ) {
                            proc_float = 0.0f;
                        }
                        int proc = (int)(proc_float + 0.5f);
                        double from_grid = boiler_energy * 0.001 * 0.01 * ( 100.0 - proc_float);
                        String txt = String.format( "%.2f kWh / %dh %02dmin / eff.: %d%% / grid: %.2f kWh", boiler_energy * 0.001, hours, minutes, proc, from_grid );
                        lblInfo.setText( txt );
                        JSONArray modes = jInfo.getJSONArray( "modes" );
                        TextView[] lblInfos;
                        lblInfos = new TextView[3];
                        lblInfos[0] = findViewById(R.id.lblBoilerInfo1);
                        lblInfos[1] = findViewById(R.id.lblBoilerInfo2);
                        lblInfos[2] = findViewById(R.id.lblBoilerInfo3);
                        Button[] btnInfos;
                        btnInfos = new Button[3];
                        btnInfos[0] = findViewById(R.id.btnBoilerColor1);
                        btnInfos[1] = findViewById(R.id.btnBoilerColor2);
                        btnInfos[2] = findViewById(R.id.btnBoilerColor3);

                        for( int iMode = 0; iMode < min( modes.length(),3 ); iMode++ ) {
                            String mode = modes.get( iMode ).toString();
                            JSONObject jMode = jInfo.getJSONObject( mode );

                            Double boiler_energy_mode = jMode.getDouble("total_boiler_energy");
                            Double consume_energy_mode = jMode.getDouble("total_consume");
                            Integer total_minutes_mode = jMode.getInt("total_minutes");
                            Integer hours_mode = total_minutes_mode / 60;
                            Integer minutes_mode = total_minutes_mode % 60;
                            //int proc_mode = (int)( ( ( boiler_energy_mode.floatValue() - consume_energy_mode.floatValue() ) / boiler_energy_mode.floatValue() ) * 100 + 0.5f );
                            String txt_mode = String.format( "%s: %.2f kWh / %dh %02dmin", mode, boiler_energy_mode * 0.001, hours_mode, minutes_mode );
                            lblInfos[iMode].setText(txt_mode);
                            int col = 0xff000000;
                            if( mode.equals("smart")) {
                                col = 0xff00a000;
                            } else if( mode.equals("force")) {
                                col = 0xffff0000;
                            } else if( mode.equals("timer")) {
                                col = 0xffa0a0a0;
                            }
                            btnInfos[iMode].setBackgroundColor(col);
                            btnInfos[iMode].setVisibility(View.VISIBLE);
                            lblInfos[iMode].setVisibility(View.VISIBLE);
                        }
                        for( int iOther = modes.length(); iOther < 3; iOther++ ) {
                            btnInfos[iOther].setVisibility(View.INVISIBLE);
                            lblInfos[iOther].setVisibility(View.INVISIBLE);
                        }

                        // draw chart
                        JSONObject jPlot = new JSONObject(( content_plot.toString() ));
                        JSONArray jTemps = jPlot.getJSONArray("temps");
                        JSONArray jTimes = jPlot.getJSONArray("times");
                        List< String > dates = new ArrayList< String >();
                        List< Double > temps = new ArrayList< Double >();
                        Double min_temp = 100.0;
                        for( int i = 0; i < jTemps.length(); i++ )
                        {
                            String str2 = jTimes.get( i ).toString();
                            dates.add( str2.substring(0,2) + ":" + str2.substring(2,4) );
                            Double next_temp = Double.valueOf(jTemps.get( i ).toString() );
                            temps.add( next_temp );
                            min_temp = min( min_temp, next_temp );
                        }
                        min_temp = max( min_temp, 20 );
                        int min_temp_int = min_temp.intValue();
                        min_temp_int -= ( min_temp_int % 5 );

                        int blue = Color.rgb( 0, 0, 255 );

                        LineChart chartBoiler = findViewById(R.id.chartBoiler);
                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                        YAxis leftAxis = chartBoiler.getAxisLeft();
                        LineDataSet set_temp = setDataForDay( chartBoiler, dates, temps );
                        //set_temp.setFillColor(red);
                        set_temp.setColor( blue );
                        //set_temp.setFillAlpha(255);
                        set_temp.setDrawValues(false);
                        //set_temp.setDrawFilled(true);
                        leftAxis.setAxisMinimum((float)(min_temp_int));

                        JSONArray jLogging = jInfo.getJSONArray("logging");
                        for( int iLog = 0; iLog < jLogging.length(); iLog++ ) {
                            JSONObject jLog = jLogging.getJSONObject(iLog);
                            String start_time = jLog.getString("start_time");
                            String end_time = jLog.getString("end_time");
                            String mode = jLog.getString("mode");
                            int tm1 = Integer.valueOf(start_time.substring(0,2)) * 60 + Integer.valueOf(start_time.substring(3,5));
                            int tm2 = Integer.valueOf(end_time.substring(0,2)) * 60 + Integer.valueOf(end_time.substring(3,5));
                            List< String > dates_next = new ArrayList< String >();
                            List< Double > temps_next = new ArrayList< Double >();
                            for( int iTime = 0; iTime < dates.size() - 1; iTime++ ) {
                                int nextTime1 = Integer.valueOf(dates.get(iTime).substring(0,2)) * 60 + Integer.valueOf(dates.get(iTime).substring(3,5));
                                int nextTime2 = Integer.valueOf(dates.get(iTime+1).substring(0,2)) * 60 + Integer.valueOf(dates.get(iTime+1).substring(3,5));
                                int time_min = java.lang.Integer.max( nextTime1, tm1 );
                                int time_max = java.lang.Integer.min( nextTime2, tm2 );
                                if( time_min <= time_max ) {
                                    dates_next.add( dates.get( iTime ) );
                                    temps_next.add( temps.get( iTime ) );
                                }
                            }
                            if( dates.size() >= 2 ) {
                                LineDataSet set_next = setDataForDay( chartBoiler, dates_next, temps_next );
                                int col = 0xff000000;
                                if( mode.equals("smart")) {
                                    col = 0xff00a000;
                                } else if( mode.equals("force")) {
                                    col = 0xffff0000;
                                } else if( mode.equals("timer")) {
                                    col = 0xffa0a0a0;
                                }
                                set_next.setFillColor(col);
                                set_next.setColor( blue );
                                set_next.setFillAlpha(255);
                                set_next.setDrawValues(false);
                                set_next.setDrawFilled(true);
                                dataSets.add(set_next);
                            }
                        }

                        dataSets.add(set_temp);
                        LineData data = new LineData(dataSets);

                        chartBoiler.getAxisRight().setEnabled(false);
                        chartBoiler.setData(data);
                        chartBoiler.notifyDataSetChanged();
                        chartBoiler.invalidate();

                    } catch (JSONException e) {
                        // Oops
                    }
                }
            });
        });
    }

    private void applyBoilerChanges()
    {
        final SmartPV app = (SmartPV) getApplication();
        Context context = getApplicationContext();

        EditText lbl = findViewById(R.id.numMinTemperature);
        String min_temp = lbl.getText().toString();
        lbl = findViewById(R.id.numMaxTemperature);
        String max_temp = lbl.getText().toString();

        lbl = findViewById(R.id.numMinBattery);
        String min_batt = lbl.getText().toString();
        lbl = findViewById(R.id.numMaxBattery);
        String max_batt = lbl.getText().toString();

        lbl = findViewById(R.id.numTargetForceTemperature);
        String force_temp = lbl.getText().toString();

        EditText tm = findViewById( R.id.edtTargetTime );
        String str = tm.getText().toString();
        int force_target_hour = 0;
        int force_target_minute = 0;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date date = format.parse(str);
            force_target_hour = date.getHours();
            force_target_minute = date.getMinutes();
        } catch ( ParseException e ) {

        }
        int force_target_hour1 = force_target_hour;
        int force_target_minute1 = force_target_minute;

        Switch btn = findViewById(R.id.switchSmartMode);
        Integer smart_mode_tmp = 0;
        if( btn.isChecked() ) {
            smart_mode_tmp = 1;
        }
        Integer smart_mode = smart_mode_tmp;
        btn = findViewById(R.id.switchForceMode);
        Integer force_mode_tmp = 0;
        if( btn.isChecked() ) {
            force_mode_tmp = 1;
        }
        Integer force_mode = force_mode_tmp;

        btn = findViewById(R.id.switchBoilerState);
        String boiler_state = "OFF";
        if( btn.isChecked() ) {
            boiler_state = "ON";
        }
        String boiler_state1 = boiler_state;

        app.getExecutors().networkIO().execute(() -> {
            String baseUrl = "http://nas8055.synology.me:50111/rest/set_boiler?name=admin&password=citroen2020!" +
                    "&mode=4" +
                    "&min_temperature=" + min_temp +
                    "&max_temperature=" + max_temp +
                    "&min_battery=" + min_batt +
                    "&max_battery=" + max_batt +
                    "&smart_mode=" + Integer.toString( smart_mode ) +
                    "&force_mode=" + Integer.toString(force_mode) +
                    "&force_target_time_hour=" + Integer.toString(force_target_hour1) +
                    "&force_target_time_minute=" + Integer.toString(force_target_minute1) +
                    "&force_target_temperature=" + force_temp +
                    "&energy_state=" + boiler_state1;
            try {
                URL filterUrl = new URL(baseUrl);
                HttpURLConnection connection = (HttpURLConnection) filterUrl.openConnection();
                connection.setReadTimeout(10000); // 10 sec
                connection.setConnectTimeout(10000); // 10 sec
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();
                int statusCode = connection.getResponseCode();
                if (statusCode != 200) {
                }

                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                is.close();
                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    public void drawDailyPlot( LineChart volumeReportChart, StringBuilder content )
    {
        try {
            // draw daily plot
            JSONArray jArray_plot = new JSONArray(( content.toString() ));
            JSONObject jObject_plot = jArray_plot.getJSONObject(0);
            JSONArray jP = jObject_plot.getJSONArray("p_arr");
            JSONArray jTotal = jObject_plot.getJSONArray("e_total_arr");
            JSONArray jTime = jObject_plot.getJSONArray("time_arr" );
            JSONArray jConsume = jObject_plot.getJSONArray( "consume_arr" );
            JSONArray jProduce = jObject_plot.getJSONArray( "produce_arr" );
            JSONArray jLoad = jObject_plot.getJSONArray( "p_load_arr" );
            JSONArray jBattery = jObject_plot.getJSONArray("battery_arr");
            Double pv_today = jObject_plot.getDouble( "e_total" ) * 0.001;
            List< String > dates = new ArrayList< String >();
            List< Double > amounts = new ArrayList< Double >();
            List< Double > self_consumes = new ArrayList< Double >();
            List< Double > consumes = new ArrayList< Double >();
            List< Double > batteries_in = new ArrayList< Double >();
            List< Double > batteries_out = new ArrayList< Double >();
            List< Double > batteries_plot = new ArrayList< Double >();
            Double prev_consume = Double.valueOf(jConsume.get( 0 ).toString() ) * 0.001;
            Double prev_produce = Double.valueOf(jProduce.get( 0 ).toString() ) * 0.001;
            Double prev_batt_proc = Double.valueOf(jBattery.get( 0 ).toString() );
            String str_time0 = jTime.get( 0 ).toString();
            Integer prev_time = Integer.parseInt(String.valueOf(str_time0.substring(0,2))) * 60 + Integer.parseInt(String.valueOf(str_time0.substring(2,4)));
            for( int i = 0; i < jP.length(); i++ )
            {
                String str2 = jTime.get( i ).toString();
                Integer newTime = Integer.parseInt(String.valueOf(str2.substring(0,2))) * 60 + Integer.parseInt(String.valueOf(str2.substring(2,4)));
                Double timeFactor = 0.0;
                if( newTime != prev_time ) {
                    Double timeDiffMin = Double.valueOf( newTime - prev_time );
                    timeFactor = 60.0 / timeDiffMin;
                }
                dates.add( str2.substring(0,2) + ":" + str2.substring(2,4) );
                Double next_consume = Double.valueOf(jConsume.get( i ).toString() ) * 0.001;
                Double next_produce = Double.valueOf(jProduce.get( i ).toString() ) * 0.001;
                Double next_p = Double.valueOf(jP.get( i ).toString() ) * 0.001 * mTotalFactor;
                Double next_batt_proc = Double.valueOf(jBattery.get( i ).toString() );
                Double batt2 = ( next_batt_proc - prev_batt_proc ) * 0.01 * 5.120 * timeFactor;
                Double consume2 = (next_consume - prev_consume) * timeFactor;
                Double produce2 = (next_produce - prev_produce ) * timeFactor;

                Double total2 = next_p;// / timeFactor;

                amounts.add( total2 );
                consumes.add( consume2 + ( total2 - produce2 ) );
                Double self2 = total2 - produce2;
                if( ( next_batt_proc >= 5.0 ) && ( batt2 > 0 ) ) {
                    self2 -= batt2;
                    consumes.set( consumes.size() - 1, consumes.get( consumes.size() - 1 ) - batt2 );
                }
                if( ( next_batt_proc >= 5.0 ) && ( batt2 < 0 ) ) {
                    //self2 -= batt2;
                    consumes.set( consumes.size() - 1, consumes.get( consumes.size() - 1 ) - batt2 );
                }
                self_consumes.add( self2 );
                if( ( next_batt_proc >= 5.0 ) && ( prev_batt_proc >= 5.0 ) ) {
                    if( batt2 > 0 ) {
                        batteries_in.add( self2 + batt2 );
                        batteries_out.add(0.0);
                    } else {
                        batteries_out.add( self2 - batt2 );
                        batteries_in.add(0.0);
                    }
                } else {
                    batteries_in.add(0.0);
                    batteries_out.add(0.0);
                }

                batteries_plot.add( next_batt_proc );

                prev_produce = next_produce;
                prev_consume = next_consume;
                prev_time = newTime;
                prev_batt_proc = next_batt_proc;
            }

            if( mLayoutCurrent.getVisibility() == View.VISIBLE ) {
                TextView lbl_pv = findViewById(R.id.lblProductionToday);
                String str = String.format("%.2f kWh", pv_today );
                lbl_pv.setText( str );
            }

            int red = Color.rgb(255, 64, 0);
            int orange = Color.rgb(255, 165, 0);
            int orange2 = Color.rgb( 255, 230, 70 );
            int blue = Color.rgb( 0, 0, 255 );
            int green_in = 0xff00b000;
            int green_out = 0xff008000;
            int green_plot = 0xff9932CC;
            //int green_plot = 0xff8B8000;
            int red2 = Color.rgb( 255, 0, 0 );

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            YAxis leftAxis = volumeReportChart.getAxisLeft();
            YAxis rightAxis = volumeReportChart.getAxisRight();
            if( mIndexPlotDaily == 0 )  {
                rightAxis.setEnabled(true);
                showColorButtons("normal");
                leftAxis.setAxisMinimum(0f);

                LineDataSet set_p = setDataForDay( volumeReportChart, dates, amounts );
                set_p.setColor( red );
                set_p.setFillColor(red);
                set_p.setFillAlpha(255);
                set_p.setDrawValues(false);
                set_p.setDrawFilled(true);

                LineDataSet set_consume = setDataForDay( volumeReportChart, dates, consumes );
                set_consume.setColor( blue );
                set_consume.setFillAlpha(255);
                set_consume.setDrawValues(false);
                set_consume.setDrawFilled(false);

                LineDataSet set_battery_in = setDataForDay( volumeReportChart, dates, batteries_in );
                set_battery_in.setColor(green_in);
                set_battery_in.setFillColor(green_in);
                set_battery_in.setFillAlpha(255);
                set_battery_in.setDrawValues(false);
                set_battery_in.setDrawFilled(true);

                LineDataSet set_battery_out = setDataForDay( volumeReportChart, dates, batteries_out );
                set_battery_out.setColor(green_out);
                set_battery_out.setFillColor(green_out);
                set_battery_out.setFillAlpha(255);
                set_battery_out.setDrawValues(false);
                set_battery_out.setDrawFilled(true);

                LineDataSet set_self = setDataForDay( volumeReportChart, dates, self_consumes );
                set_self.setColor(orange);
                set_self.setFillColor(orange);
                set_self.setFillAlpha(255);
                set_self.setDrawValues(false);
                set_self.setDrawFilled(true);

                LineDataSet set_p2 = setDataForDay( volumeReportChart, dates, amounts );
                set_p2.setColor( red2 );
                set_p2.setFillAlpha(255);
                set_p2.setDrawValues(false);
                set_p2.setDrawFilled(false);

                LineDataSet set_battery = setDataForDay( volumeReportChart, dates, batteries_plot );
                set_battery.setColor( green_plot );
                set_battery.setFillAlpha(255);
                set_battery.setDrawValues(false);
                set_battery.setDrawFilled(false);
                set_battery.setAxisDependency(YAxis.AxisDependency.RIGHT);

                dataSets.add(set_p);
                dataSets.add(set_battery_in);
                dataSets.add(set_battery_out);
                dataSets.add(set_self);
                dataSets.add(set_battery);
                dataSets.add(set_consume);
                dataSets.add(set_p2);
                leftAxis.setAxisMinimum(0f);

                leftAxis.setLabelCount(6, true);
                rightAxis.setLabelCount(6, true);
            } else if( mIndexPlotDaily == 1 ) {
                rightAxis.setEnabled(false);
                showColorButtons("none");
                float min_value = 0.0f;
                for( int i = 0; i < amounts.size(); i++ ) {
                    Double next = amounts.get(i) - max( batteries_in.get(i), batteries_out.get(i));
                    if( max( batteries_in.get(i), batteries_out.get(i)) < 0.05) {
                        next -= consumes.get(i);
                    }
                    next = max( amounts.get(i), batteries_out.get(i) ) - max( batteries_in.get(i), consumes.get(i) );
                    amounts.set( i, next );
                    min_value = min( min_value, next.floatValue() );
                }
                leftAxis.setAxisMinimum(min_value);
                LineDataSet set_diff = setDataForDay( volumeReportChart, dates, amounts );
                set_diff.setColor( blue );
                set_diff.setFillColor( green_in );
                set_diff.setFillAlpha(255);
                set_diff.setDrawValues(false);
                set_diff.setDrawFilled(true);
                dataSets.add(set_diff);
                leftAxis.setLabelCount(6, false);
            }

            LineData data = new LineData(dataSets);

            volumeReportChart.setData(data);
            rightAxis.setAxisMaximum(100.0f);

            volumeReportChart.notifyDataSetChanged();
            volumeReportChart.invalidate();

        } catch (JSONException e) {
            // Oops
        }
    }

    public int drawBarPlot( BarChart volumeReportChart, StringBuilder content, Integer start_day, Integer max_days )
    {
        int num_values = 0;
        try {
            // draw daily plot
            JSONArray jArray_plot = new JSONArray(( content.toString() ));
            JSONObject jObject = jArray_plot.getJSONObject(0);

            Boolean get_next = Boolean.TRUE;
            List< Double > bars = new ArrayList< Double >();
            List< Double > totals = new ArrayList< Double >();
            List< Double > self_consumes = new ArrayList< Double >();
            List< Double > consumes = new ArrayList< Double >();
            List< Double > earn_consumes = new ArrayList< Double >();
            List< Double > earn_produces = new ArrayList< Double >();
            List< Double > money_spent = new ArrayList< Double >();
            for( int i_next = start_day; i_next <= max_days; i_next++ )
            {
                try {
                    String str = String.format("%02d", i_next);
                    JSONObject jNext = jObject.getJSONObject(str);

                    Double total = jNext.getDouble("e_total") * mTotalFactor * 0.001;
                    Double consume = jNext.getDouble( "consume" ) * 0.001;
                    Double produce = jNext.getDouble( "produce" ) * 0.001;
                    Double self_consume = total - produce;

                    Double earn_consume = jNext.getDouble( "earn_consume" );
                    Double earn_produce = jNext.getDouble( "earn_produce" );
                    Double spent = jNext.getDouble( "money_spent" );
                    earn_consumes.add( earn_consume );
                    earn_produces.add( earn_produce );
                    money_spent.add( spent );

                    double d_next = i_next;
                    bars.add(d_next);
                    totals.add( total );
                    consumes.add( consume + self_consume );
                    self_consumes.add( self_consume );
                    num_values+=1;
                } catch( JSONException e ) {
                    Double total = 0.0;
                    Double consume = 0.0;
                    Double produce = 0.0;
                    Double self_consume = 0.0;

                    double d_next = i_next;
                    bars.add(d_next);
                    totals.add( total );
                    consumes.add( consume + self_consume );
                    self_consumes.add( self_consume );

                    earn_consumes.add( 0.0 );
                    earn_produces.add( 0.0 );
                    money_spent.add( 0.0 );
                }
            }

            int red = Color.rgb(255, 128, 0);
            int orange = Color.rgb(255, 165, 0);
            int orange2 = Color.rgb( 255, 230, 70 );
            int blue = Color.rgb( 0, 0, 255 );
            int green = Color.rgb( 128, 128, 128);
            int red2 = Color.rgb( 255, 0, 0 );

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();

            ArrayList<BarEntry> values = new ArrayList<>();
            if( mIndexPlotBar == 0 ) {
                showColorButtons("normal");
                ArrayList<BarEntry> values_consume = new ArrayList<>();
                for (int i = 0; i < bars.size(); i++) {
                    float value1 = self_consumes.get(i).floatValue();
                    float value2 = totals.get(i).floatValue();
                    values.add(new BarEntry(bars.get(i).floatValue(), new float[]{value1, value2 - value1}));
                    values_consume.add(new BarEntry(bars.get(i).floatValue(), consumes.get(i).floatValue()));
                }

                BarDataSet set1 = new BarDataSet(values, "1");
                set1.setColors(new int[]{orange, red2});

                BarDataSet set_consume = new BarDataSet(values_consume, "2");
                set_consume.setColors(blue);

                dataSets.add(set1);
                dataSets.add(set_consume);

                BarData data = new BarData(dataSets);
                data.setDrawValues(false);
                float bar_space = 0.05f;
                float bar_width = 0.35f;
                data.setBarWidth(0.35f);
                float groupSpace = 1f - ((bar_space + bar_width) * 2);
                volumeReportChart.setData(data);
                volumeReportChart.groupBars(0, groupSpace, bar_space);

            } else if( mIndexPlotBar == 1 ) {
                showColorButtons("money" );
                ArrayList<BarEntry> values_spent = new ArrayList<>();
                for (int i = 0; i < bars.size(); i++) {
                    float value1 = earn_produces.get(i).floatValue();
                    float value2 = earn_consumes.get(i).floatValue();
                    bars.set( i, bars.get( i ) - 0.5 );
                    values.add(new BarEntry(bars.get(i).floatValue(), new float[]{value1, value2}));
                    values_spent.add(new BarEntry(bars.get(i).floatValue(), money_spent.get(i).floatValue()));
                }
                BarDataSet set1 = new BarDataSet(values, "1");
                set1.setColors(new int[]{red2, orange});

                BarDataSet set_spent = new BarDataSet(values_spent, "2");
                set_spent.setColors(blue);

                dataSets.add(set1);
                dataSets.add(set_spent);
                BarData data = new BarData(dataSets);
                data.setDrawValues(false);
                float bar_space = 0.05f;
                float bar_width = 0.35f;
                data.setBarWidth(0.35f);
                float groupSpace = 1f - ((bar_space + bar_width) * 2);
                volumeReportChart.setData(data);
                volumeReportChart.groupBars(0, groupSpace, bar_space);
            }

            volumeReportChart.notifyDataSetChanged();
            volumeReportChart.invalidate();

        } catch (JSONException e) {
            // Oops
        }
        return num_values;
    }

    private StringBuilder getREST( String baseUrl ) {
        StringBuilder content = new StringBuilder();
        try {
            URL filterUrl = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) filterUrl.openConnection();
            connection.setReadTimeout(10000); // 10 sec
            connection.setConnectTimeout(10000); // 10 sec
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            is.close();
            connection.disconnect();
        } catch (
                MalformedURLException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    private void setupPlotDay( LineChart volumeReportChart) {
        volumeReportChart.setTouchEnabled(true);
        volumeReportChart.setPinchZoom(true);
        volumeReportChart.setDrawMarkers(false);
        volumeReportChart.getLegend().setEnabled(false);

        XAxis xAxis = volumeReportChart.getXAxis();
        xAxis.setCenterAxisLabels(false);
        YAxis leftAxis = volumeReportChart.getAxisLeft();
        YAxis rightAxis = volumeReportChart.getAxisRight();
        rightAxis.setEnabled(true);

        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);

        volumeReportChart.getDescription().setEnabled(false);

        xAxis.setLabelRotationAngle(0f);
        xAxis.setDrawLimitLinesBehindData(false);

        xAxis.setAxisMinimum( 0.0f );
        xAxis.setAxisMaximum( 24 * 60 * 60 * 1000 );
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        xAxis.setShowSpecificLabelPositions(true);
        xAxis.setSpecificLabelPositions( new float[]{0, 3*3600000, 6*3600000, 9*3600000, 12*3600000, 15*3600000, 18*3600000, 21*3600000, 24*3600000});

        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(true);
        leftAxis.setValueFormatter(new ClaimsYAxis());

        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setDrawLimitLinesBehindData(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(true);
        //rightAxis.setValueFormatter(new ClaimsYAxis());

    }

    private void setupPlotBar( BarChart volumeReportChart, Integer max_value) {
        volumeReportChart.setTouchEnabled(true);
        volumeReportChart.setPinchZoom(true);
        volumeReportChart.setDrawMarkers(false);
        volumeReportChart.getLegend().setEnabled(false);

        YAxis rightAxis = volumeReportChart.getAxisRight();
        rightAxis.setEnabled(false);

        XAxis xAxis = volumeReportChart.getXAxis();
        if( mHistoryMode == "week" ) {
            xAxis.setValueFormatter(new ClaimsXAxisWeek());
            xAxis.setCenterAxisLabels(true);
        } else if( mHistoryMode == "year" ) {
            xAxis.setValueFormatter(new ClaimsXAxisYear());
            xAxis.setCenterAxisLabels(true);
        } else if( mHistoryMode == "total" ) {
            xAxis.setValueFormatter(new ClaimsXAxisTotal());
            xAxis.setCenterAxisLabels(true);
        } else {
            xAxis.setValueFormatter(new ClaimsXAxisBar());
            xAxis.setCenterAxisLabels(false);
        }
        xAxis.setGranularity(1f);
        YAxis leftAxis = volumeReportChart.getAxisLeft();

        XAxis.XAxisPosition position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setPosition(position);

        volumeReportChart.getDescription().setEnabled(false);

        xAxis.setLabelRotationAngle(0f);

        xAxis.setDrawLimitLinesBehindData(false);

        xAxis.setAxisMinimum( 0.0f );
        xAxis.setAxisMaximum( max_value );
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);
        Integer num_bars = max_value;
        if( num_bars > 15 ) {
            num_bars /= 2;
        }
        xAxis.setLabelCount(num_bars);

        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(true);
        leftAxis.setValueFormatter(new ClaimsYAxis());

        rightAxis.setDrawZeroLine(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);
    }

    public long getDateInMilliSeconds(String givenDateString, String format) {
        String DATE_TIME_FORMAT = format;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        long timeInMilliseconds = 1;
        try {
            Date mDate = sdf.parse(givenDateString);
            timeInMilliseconds = mDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeInMilliseconds;
    }

    private LineDataSet setDataForDay( LineChart volumeReportChart, List<String> times, List<Double> amounts) {

        XAxis xAxis = volumeReportChart.getXAxis();
        xAxis.setValueFormatter(new ClaimsXAxisDay(times));

        ArrayList<Entry> values = new ArrayList<>();
        for( Integer i = 0; i < amounts.size(); i++ ) {
            values.add(new Entry(getDateInMilliSeconds(times.get(i), "HH:mm"), amounts.get(i).floatValue()));
        }

        LineDataSet set1;

        set1 = new LineDataSet(values, "");
        set1.setDrawCircles(false);

        set1.setLineWidth(2f);//line size
        set1.setValueTextSize(10f);
        set1.setFormLineWidth(5f);
        set1.setFormSize(5.f);

        return set1;
    }

    private void showAverages( Boolean show ) {
        int vis = View.VISIBLE;
        if( !show ) {
            vis = View.INVISIBLE;
        }
        TextView lbl = findViewById(R.id.lblHistoryAvgProduction);
        lbl.setVisibility(vis);
        lbl = findViewById(R.id.lblHistoryAvgProductionText);
        lbl.setVisibility(vis);
        lbl = findViewById(R.id.lblHistoryAvgConsumption);
        lbl.setVisibility(vis);
        lbl = findViewById(R.id.lblHistoryAvgConsumptionText);
        lbl.setVisibility(vis);
        lbl = findViewById(R.id.lblHistoryAvgSelfConsumption);
        lbl.setVisibility(vis);
        lbl = findViewById(R.id.lblHistoryAvgSelfConsumptionText);
        lbl.setVisibility(vis);
    }

    private void showAvgStatistic( int num_values ) {
        if( num_values < 1 ) {
            return;
        }
        Double d_num = Double.valueOf(num_values);
        TextView lbl = findViewById(R.id.lblHistoryProduction);
        String ss = lbl.getText().toString();
        ss = ss.replace( " kWh", "");
        Double total = Double.parseDouble( ss );
        lbl = findViewById(R.id.lblHistoryConsumption);
        ss = lbl.getText().toString();
        ss = ss.replace( " kWh", "");
        Double consume = Double.parseDouble(ss);
        lbl = findViewById(R.id.lblHistorySelfConsumption);
        ss = lbl.getText().toString();
        ss = ss.replace( " kWh", "");
        Double self_consume = Double.parseDouble(ss);
        Double consume_total = consume + self_consume;

        Double avg_prod = total / d_num;
        lbl = findViewById(R.id.lblHistoryAvgProduction);
        String str = String.format( "%.2f kWh", avg_prod );
        lbl.setText( str );

        Double avg_cons = avg_cons = consume / d_num;
        lbl = findViewById(R.id.lblHistoryAvgConsumption);
        str = String.format( "%.2f kWh", avg_cons );
        lbl.setText( str );

        Double avg_self_cons = self_consume / d_num;
        lbl = findViewById(R.id.lblHistoryAvgSelfConsumption);
        str = String.format( "%.2f kWh", avg_self_cons );
        lbl.setText( str );
    }

    public void showColorButtons( String mode )  // normal, money, none
    {
        int vis_normal = TextView.VISIBLE;
        int vis_money = TextView.VISIBLE;
        if( !mode.equals("normal") ) {
            vis_normal = TextView.INVISIBLE;
        }
        if( !mode.equals("money") ) {
            vis_money = TextView.INVISIBLE;
        }
        TextView lblProduction = findViewById(R.id.btnProductionColor);
        lblProduction.setVisibility( vis_normal );
        TextView lblConsumption = findViewById(R.id.btnConsumptionColor);
        lblConsumption.setVisibility( vis_normal );
        TextView lblSelfConsumption = findViewById(R.id.btnSelfConsumptionColor);
        lblSelfConsumption.setVisibility( vis_normal );

        TextView lblSold = findViewById(R.id.btnSoldColor);
        lblSold.setVisibility( vis_money );
        TextView lblSaved = findViewById(R.id.btnSavedColor);
        lblSaved.setVisibility( vis_money );
        TextView lblSpent = findViewById(R.id.btnSpentColor);
        lblSpent.setVisibility( vis_money );

        TextView lblBattery = findViewById(R.id.btnBatteryColor);
        lblBattery.setVisibility( vis_normal );

        if( mHistoryMode != "day" ) {
            lblBattery.setVisibility(View.INVISIBLE);
        }
    }

    public String getWindIcon( Integer dir_int ) {
        if( dir_int == 0 ) return "none";
        float dir = (float)(dir_int);
        if( ( dir < 22.5f ) || ( dir > 337.5f ) ) return "down";
        if( dir < 67.5f ) return "left_down";
        if( dir < 112.5f ) return "left";
        if( dir < 157.5 ) return "left_up";
        if( dir < 202.5f ) return "up";
        if( dir < 247.5f ) return "right_up";
        if( dir < 292.5f ) return "right";
        return "right_down";
    }

    public void initialiseWeatherArrays() {
        mArTimes = new TextView[6];
        mArTimes[0] = findViewById(R.id.lblWeatherTime1);
        mArTimes[1] = findViewById(R.id.lblWeatherTime2);
        mArTimes[2] = findViewById(R.id.lblWeatherTime3);
        mArTimes[3] = findViewById(R.id.lblWeatherTime4);
        mArTimes[4] = findViewById(R.id.lblWeatherTime5);
        mArTimes[5] = findViewById(R.id.lblWeatherTime6);

        mArIcons = new ImageView[6];
        mArIcons[0] = findViewById(R.id.imageWeatherIcon1);
        mArIcons[1] = findViewById(R.id.imageWeatherIcon2);
        mArIcons[2] = findViewById(R.id.imageWeatherIcon3);
        mArIcons[3] = findViewById(R.id.imageWeatherIcon4);
        mArIcons[4] = findViewById(R.id.imageWeatherIcon5);
        mArIcons[5] = findViewById(R.id.imageWeatherIcon6);

        mArTemps = new TextView[6];
        mArTemps[0] = findViewById(R.id.lblWeatherTemp1);
        mArTemps[1] = findViewById(R.id.lblWeatherTemp2);
        mArTemps[2] = findViewById(R.id.lblWeatherTemp3);
        mArTemps[3] = findViewById(R.id.lblWeatherTemp4);
        mArTemps[4] = findViewById(R.id.lblWeatherTemp5);
        mArTemps[5] = findViewById(R.id.lblWeatherTemp6);

        mArSky = new TextView[6];
        mArSky[0] = findViewById(R.id.lblWeatherSky1);
        mArSky[1] = findViewById(R.id.lblWeatherSky2);
        mArSky[2] = findViewById(R.id.lblWeatherSky3);
        mArSky[3] = findViewById(R.id.lblWeatherSky4);
        mArSky[4] = findViewById(R.id.lblWeatherSky5);
        mArSky[5] = findViewById(R.id.lblWeatherSky6);

        mArPop = new TextView[6];
        mArPop[0] = findViewById(R.id.lblWeatherPop1);
        mArPop[1] = findViewById(R.id.lblWeatherPop2);
        mArPop[2] = findViewById(R.id.lblWeatherPop3);
        mArPop[3] = findViewById(R.id.lblWeatherPop4);
        mArPop[4] = findViewById(R.id.lblWeatherPop5);
        mArPop[5] = findViewById(R.id.lblWeatherPop6);

        mArWind = new ImageView[6];
        mArWind[0] = findViewById(R.id.imageWeatherWind1);
        mArWind[1] = findViewById(R.id.imageWeatherWind2);
        mArWind[2] = findViewById(R.id.imageWeatherWind3);
        mArWind[3] = findViewById(R.id.imageWeatherWind4);
        mArWind[4] = findViewById(R.id.imageWeatherWind5);
        mArWind[5] = findViewById(R.id.imageWeatherWind6);

        mArWindSpeed = new TextView[6];
        mArWindSpeed[0] = findViewById(R.id.lblWeatherWindSpeed1);
        mArWindSpeed[1] = findViewById(R.id.lblWeatherWindSpeed2);
        mArWindSpeed[2] = findViewById(R.id.lblWeatherWindSpeed3);
        mArWindSpeed[3] = findViewById(R.id.lblWeatherWindSpeed4);
        mArWindSpeed[4] = findViewById(R.id.lblWeatherWindSpeed5);
        mArWindSpeed[5] = findViewById(R.id.lblWeatherWindSpeed6);

        mArPrediction = new TextView[6];
        mArPrediction[0] = findViewById(R.id.lblWeatherPrediction1);
        mArPrediction[1] = findViewById(R.id.lblWeatherPrediction2);
        mArPrediction[2] = findViewById(R.id.lblWeatherPrediction3);
        mArPrediction[3] = findViewById(R.id.lblWeatherPrediction4);
        mArPrediction[4] = findViewById(R.id.lblWeatherPrediction5);
        mArPrediction[5] = findViewById(R.id.lblWeatherPrediction6);
    }

    public void resetWeatherArrays() {
        for( int i = 0; i < 6; i++ ) {
            mArTimes[i].setText("");
            mArIcons[i].setImageDrawable(null);
            mArTemps[i].setText("");
            mArSky[i].setText("");
            mArPop[i].setText("");
            mArWind[i].setImageDrawable(null);
            mArWindSpeed[i].setText("");
            mArPrediction[i].setText("");
        }
    }
}
