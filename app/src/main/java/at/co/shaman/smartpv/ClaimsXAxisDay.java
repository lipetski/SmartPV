package at.co.shaman.smartpv;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ClaimsXAxisDay implements IAxisValueFormatter {

    List<String> datesList;

    public ClaimsXAxisDay(List<String> arrayOfDates) {
        this.datesList = arrayOfDates;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
/*
Depends on the position number on the X axis, we need to display the label, Here, this is the logic to convert the float value to integer so that I can get the value from array based on that integer and can convert it to the required value here, month and date as value. This is required for my data to show properly, you can customize according to your needs.
*/
        // Create a DateFormatter object for displaying date in specified format

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        Integer val = Math.round(value);

        if( val == 24 * 60 * 60 * 1000 ){
            return "24:00";
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        calendar.setTimeInMillis(val);
        TimeZone t = calendar.getTimeZone();
        String s = formatter.format(calendar.getTime());
        return formatter.format(calendar.getTime());
    }

}
