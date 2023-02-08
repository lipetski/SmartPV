package at.co.shaman.smartpv;

import com.github.mikephil.charting.components.AxisBase;
//import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClaimsXAxisWeek implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
/*
Depends on the position number on the X axis, we need to display the label, Here, this is the logic to convert the float value to integer so that I can get the value from array based on that integer and can convert it to the required value here, month and date as value. This is required for my data to show properly, you can customize according to your needs.
*/
        // Create a DateFormatter object for displaying date in specified format.
        Integer val = Math.round(value + 1.0f);
        if( val == 1 ) return "Mon";
        if( val == 2 ) return "Tue";
        if( val == 3 ) return "Wed";
        if( val == 4 ) return "Thu";
        if( val == 5 ) return "Fri";
        if( val == 6 ) return "Sat";
        if( val == 7 ) return "Sun";
        return val.toString();
    }

}
