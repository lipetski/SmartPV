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

public class ClaimsXAxisBar implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
/*
Depends on the position number on the X axis, we need to display the label, Here, this is the logic to convert the float value to integer so that I can get the value from array based on that integer and can convert it to the required value here, month and date as value. This is required for my data to show properly, you can customize according to your needs.
*/
        // Create a DateFormatter object for displaying date in specified format.
        Integer val = Math.round(value + 1.0f);
        return val.toString();
    }

}
