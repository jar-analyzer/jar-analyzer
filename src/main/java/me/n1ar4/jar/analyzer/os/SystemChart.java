package me.n1ar4.jar.analyzer.os;

import me.n1ar4.jar.analyzer.gui.MainForm;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class SystemChart extends JFrame {
    private final XYSeries cpuSeries;
    private final XYSeries memorySeries;
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private long[] prevTicks;

    public SystemChart(String title) {
        super(title);
        SystemInfo systemInfo = new SystemInfo();
        processor = systemInfo.getHardware().getProcessor();
        memory = systemInfo.getHardware().getMemory();
        prevTicks = processor.getSystemCpuLoadTicks();
        cpuSeries = new XYSeries("CPU Usage");
        memorySeries = new XYSeries("Memory Usage");
        XYSeriesCollection cpuDataset = new XYSeriesCollection(cpuSeries);
        XYSeriesCollection memoryDataset = new XYSeriesCollection(memorySeries);
        JFreeChart cpuChart = createChart(cpuDataset, "CPU Usage");
        JFreeChart memoryChart = createChart(memoryDataset, "Memory Usage");
        ChartPanel cpuChartPanel = new ChartPanel(cpuChart);
        ChartPanel memoryChartPanel = new ChartPanel(memoryChart);
        cpuChartPanel.setPreferredSize(new Dimension(800, 400));
        memoryChartPanel.setPreferredSize(new Dimension(800, 400));
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));
        panel.add(cpuChartPanel);
        panel.add(memoryChartPanel);
        setContentPane(panel);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int time = 0;

            @Override
            public void run() {
                double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                prevTicks = processor.getSystemCpuLoadTicks();
                long usedMemory = memory.getTotal() - memory.getAvailable();
                double memoryUsage = (double) usedMemory / memory.getTotal() * 100;
                cpuSeries.add(time, cpuLoad);
                memorySeries.add(time, memoryUsage);
                time += 1;
                if (cpuSeries.getItemCount() > 100) {
                    cpuSeries.remove(0);
                    memorySeries.remove(0);
                }
            }
        }, 0, 500);
    }

    private JFreeChart createChart(XYSeriesCollection dataset, String title) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Time (s)",
                "Usage (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.BLACK);
        return chart;
    }

    public static void start0() {
        SwingUtilities.invokeLater(() -> {
            SystemChart example = new SystemChart("System Usage Chart");
            example.setSize(800, 800);
            example.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
            example.setVisible(true);
        });
    }
}
