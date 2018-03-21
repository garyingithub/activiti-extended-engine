package cn.edu.sysu.workflow.cloud.load.graph;

import cn.edu.sysu.workflow.cloud.load.algorithm.scheduling.Scheduler;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
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
//        for(int i = 0; i < history.length; i++) {
//            XYSeries series = new XYSeries(String.valueOf(i));
//            for(int j = 0; j < 20; j++) {
//                series.add(j, history[i][j]);
//            }
//            collection.addSeries(series);
//        }
//
//        return collection;
    }

    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Line Chart Demo 4",      // chart title
                "X",                      // x axis label
                "Y",                      // y axis label
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
}
