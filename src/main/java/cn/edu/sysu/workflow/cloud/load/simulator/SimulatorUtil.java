package cn.edu.sysu.workflow.cloud.load.simulator;

import cn.edu.sysu.workflow.cloud.load.Main;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.Activiti;
import cn.edu.sysu.workflow.cloud.load.engine.activiti.StringCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Component
public class SimulatorUtil {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    long parseTimeStampString(String timeStampString) {
        timeStampString=timeStampString.substring(0,timeStampString.length()-10);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date timeStamp;
        try {
            timeStamp = format.parse(timeStampString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (timeStamp == null) {
            return 0;
        }
        return timeStamp.getTime();
    }

    public void scanAndUploadDefinitions(Activiti activiti) {
        File processDirectory = new File(Main.class.getClassLoader().getResource("processes").getPath());
        File[] processDefinitionFiles = processDirectory.listFiles();

        logger.info("There are {} engine definitions in directory", processDefinitionFiles.length);
        Arrays.stream(processDefinitionFiles).forEach(file -> activiti.deployProcessDefinition(file.getName(), file, new StringCallback() {
            @Override
            public void call(String result) {

            }
        }));
    }
}
