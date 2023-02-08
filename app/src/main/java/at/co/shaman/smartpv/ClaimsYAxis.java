package at.co.shaman.smartpv;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class ClaimsYAxis implements IAxisValueFormatter {

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        String str = String.format( "%.1f", value );
        return str;
    }


}

