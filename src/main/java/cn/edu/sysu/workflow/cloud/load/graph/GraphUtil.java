package cn.edu.sysu.workflow.cloud.load.graph;

import cn.edu.sysu.workflow.cloud.load.algorithm.admit.AdmitController;
import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.Scheduler;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.function.BiConsumer;

public class GraphUtil {

    private static XYDataset createDataset(int[][] history) {
        XYSeriesCollection collection = new XYSeriesCollection();
        for(int i = 0; i < history.length; i++) {
            XYSeries series = new XYSeries(String.valueOf(i));
            for(int j = 0; j < 20; j++) {
                series.add(j, history[i][j]);
            }
            collection.addSeries(series);
        }

        return collection;
    }

    public static XYDataset createDataSet(Map<Scheduler, Map<Integer, Double>> result) {
        XYSeriesCollection collection = new XYSeriesCollection();

        result.forEach(new BiConsumer<Scheduler, Map<Integer, Double>>() {
            @Override
            public void accept(Scheduler scheduler, Map<Integer, Double> integerIntegerMap) {
                XYSeries series = new XYSeries(scheduler.getClass().getName());
                integerIntegerMap.forEach(new BiConsumer<Integer, Double>() {
                    @Override
                    public void accept(Integer integer, Double integer2) {
                        series.add(integer, integer2);
                    }
                });
                collection.addSeries(series);
            }
        });


        return collection;

    }

    public static XYDataset createDataSet2(Map<AdmitController, Map<Integer, Double>> result) {
        XYSeriesCollection collection = new XYSeriesCollection();

        result.forEach(new BiConsumer<AdmitController, Map<Integer, Double>>() {
            @Override
            public void accept(AdmitController scheduler, Map<Integer, Double> integerIntegerMap) {
                XYSeries series = new XYSeries(scheduler.getName());
                integerIntegerMap.forEach(new BiConsumer<Integer, Double>() {
                    @Override
                    public void accept(Integer integer, Double integer2) {
                        series.add(integer, integer2);
                    }
                });
                collection.addSeries(series);
            }
        });


        return collection;

    }

    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",      // chart title
                "时间(分钟)",                      // x axis label
                "资源利用率",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setUpperMargin(0.0);
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        XYLineAndShapeRenderer renderer
                = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setLegendLine(new Rectangle2D.Double(-4.0, -3.0, 8.0, 6.0));
        return chart;
    }

    public static JPanel createDemoPanel(int[][] history) {
        JFreeChart chart = createChart(createDataset(history));
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public static JPanel createDemoPanel(Map<Scheduler, Map<Integer, Double>> result) {
        JFreeChart chart = createChart(createDataSet(result));
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public static JPanel createDemoPanel2(Map<AdmitController, Map<Integer, Double>> result) {
        JFreeChart chart = createChart(createDataSet2(result));
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public static ApplicationFrame getFrame(int[][] history) {

        ApplicationFrame frame = new ApplicationFrame("t");
        JPanel chartPanel = createDemoPanel(history);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        frame.setContentPane(chartPanel);
        return frame;
    }

    public static ApplicationFrame getFrame(Map<Scheduler, Map<Integer, Double>> result) {

        ApplicationFrame frame = new ApplicationFrame("t");
        JPanel chartPanel = createDemoPanel(result);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        frame.setContentPane(chartPanel);
        return frame;
    }

    public static ApplicationFrame getFrame2(Map<AdmitController, Map<Integer, Double>> result) {

        ApplicationFrame frame = new ApplicationFrame("t");
        JPanel chartPanel = createDemoPanel2(result);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        frame.setContentPane(chartPanel);
        return frame;
    }

    public static void saveChartAsPDF(File file, JFreeChart chart, int width,
                                      int height, FontMapper mapper) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        writeChartAsPDF(out, chart, width, height, mapper);
        out.close();
    }
    /**
     * Writes a chart to an output stream in PDF format.
     *
     * @param out
     * the output stream.
     * @param chart
     * the chart.
     * @param width
     * the chart width.
     * @param height
     * the chart height.
     *
     */
    public static void writeChartAsPDF(OutputStream out, JFreeChart chart,
                                       int width, int height, FontMapper mapper) throws IOException {
        Rectangle pagesize = new Rectangle(width, height);
        Document document = new Document(pagesize, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addAuthor("JFreeChart");
            document.addSubject("Demonstration");
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, mapper);
            Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2, r2D);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
        } catch (DocumentException de) {
            System.err.println(de.getMessage());
        }
        document.close();
    }

    public static void printPDF2(Map<AdmitController, Map<Integer, Double>> data) {
        try {
            // create a chart...
            XYDataset dataset = createDataSet2(data);
            JFreeChart chart = createChart(dataset);
            // some additional chart customisation here...
            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot
                    .getRenderer();
//            renderer.setShapesVisible(true);
            // write the chart to a PDF file...
            File fileName = new File(System.getProperty("user.home")
                    + "/jfreechart1.pdf");
            System.out.println(fileName.getPath());
            saveChartAsPDF(fileName, chart, 400, 300, new DefaultFontMapper());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
