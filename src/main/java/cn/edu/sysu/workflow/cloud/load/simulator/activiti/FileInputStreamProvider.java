package cn.edu.sysu.workflow.cloud.load.simulator.activiti;

import org.activiti.bpmn.converter.util.InputStreamProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileInputStreamProvider implements InputStreamProvider {

    private File file;

    FileInputStreamProvider(File file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
