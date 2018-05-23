package cn.edu.sysu.workflow.cloud.load.graph;

import cn.edu.sysu.workflow.cloud.load.algorithm.HasName;
import cn.edu.sysu.workflow.cloud.load.algorithm.admit.AdmitController;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.ui.ApplicationFrame;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.function.BiConsumer;

public enum  UtilizationGraphUtil {
    INSTANCE;

    protected XYDataset createDataSet(Map<HasName, Map<Integer, Double>> data) {
        XYSeriesCollection collection = new XYSeriesCollection();

        data.forEach(new BiConsumer<HasName, Map<Integer, Double>>() {
            @Override
            public void accept(HasName scheduler, Map<Integer, Double> integerIntegerMap) {
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

    private JFreeChart createChart(XYDataset dataSet, GraphType type) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",      // chart title
                type.getXLabel(),                      // x axis label
                type.getYLabel(),                      // y axis label
                dataSet,                  // data
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

    private JPanel createPanel(Map<HasName, Map<Integer, Double>> result, GraphType type) {
        JFreeChart chart = createChart(createDataSet(result), type);
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public ApplicationFrame getFrame(Map<HasName, Map<Integer, Double>> result, GraphType type) {

        ApplicationFrame frame = new ApplicationFrame("t");
        JPanel chartPanel = createPanel(result, type);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        frame.setContentPane(chartPanel);
        return frame;
    }
}
