package cn.edu.sysu.workflow.cloud.load;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimulatorUtil {
    public static long parseTimeStampString(String timeStampString) {
        timeStampString=timeStampString.substring(0,timeStampString.length()-10);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeStamp = null;
        try {
            timeStamp = format.parse(timeStampString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if(timeStamp == null) return 0;
        return timeStamp.getTime();
    }
}
