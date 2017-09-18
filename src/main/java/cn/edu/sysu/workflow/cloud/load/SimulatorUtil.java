package cn.edu.sysu.workflow.cloud.load;

import cn.edu.sysu.workflow.cloud.load.process.activiti.Activiti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimulatorUtil {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public long parseTimeStampString(String timeStampString) {
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

    public void scanAndUploadDefinitions(Activiti activiti) {
        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        logger.info("There are {} process definitions in directory", processDefinitionFiles.length);
        Arrays.stream(processDefinitionFiles).forEach(file -> activiti.addProcessDefinition(file.getName(), file));
    }

}
