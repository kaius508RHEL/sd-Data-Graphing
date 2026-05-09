import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {

        final int OFFSET = 50;

        String path;
        if(args.length==0 || args[0].equals("0")) {
            path = fileChooser();
        }
        else{
            path=args[0];
        }

        int offset = 0;

        if(args.length==2){
            offset=Integer.valueOf(args[1]);
        }

        /** graph option dialog **/
        ArrayList<String> options = optionBox();

        /** load options results **/
        for(String elem : options){
            System.out.println(elem);
        }
        ObjectMapper mapper = new ObjectMapper();
        XYSeries tempGraph = new XYSeries("Time vs Temperature");
        XYSeries humidGraph = new XYSeries("Time vs Humidity");
        XYSeries gyro_depth_Graph = new XYSeries("Gyro: Time vs depth");
        XYSeries gyro_pos_Graph = new XYSeries("Gyro: Time vs position");
        XYSeries baro_pres_Graph = new XYSeries("Barometer: Time vs pressure");
        XYSeries baro_depth_Graph = new XYSeries("Barometer: Time vs depth");
        XYSeries baro_pres_depth_Graph = new XYSeries("Barometer: Pressure vs depth");
        XYSeries hall_rmp_0_Graph = new XYSeries("Hall Sensor 0: Time vs RPM");
        XYSeries hall_rmp_1_Graph = new XYSeries("Hall Sensor 1: Time vs RPM");

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                // {"t":1731719000,"sensor":"temp_humid","temp_c":23.8,"humid":42.1}
                if (line.contains("temp_humid") && (options.contains("t_temp") || options.contains("t_hum"))) {
                    Temp_humid current = mapper.readValue(line, Temp_humid.class);
                    if (current.temp_c != null && options.contains("t_temp")) {
                        tempGraph.add(current.t, current.temp_c);
                    }
                    if (current.humid != null && options.contains("t_hum")) {
                        humidGraph.add(current.t, current.humid);
                    }
                }
                // {"t":1731719000,"sensor":"gyro","x":-0.12,"y":0.03,"z":0.99}
                else if (line.contains("gyro") && (options.contains("g_depth") || options.contains("g_pos"))) {
                    Gyro current = mapper.readValue(line, Gyro.class);
                    if (current.z != null && options.contains("g_depth")) {
                        gyro_depth_Graph.add(current.t, current.z);
                    }
                    if (current.x != null && current.y != null && options.contains("g_pos")) {
                        gyro_pos_Graph.add(current.x, current.y);
                    }
                }
                // {"t":1731719000,"sensor":"barometer","pressure_pa":101325,"alt_m":56.3}
                else if (line.contains("barometer") && (options.contains("b_pres") || options.contains("b_depth") || options.contains("b_pres_depth"))) {
                    Baro current = mapper.readValue(line, Baro.class);
                    if (current.pressure_pa != null && options.contains("b_pres")) {
                        baro_pres_Graph.add(current.t, current.pressure_pa);
                    }
                    if (current.alt_m != null && options.contains("b_depth")) {
                        baro_depth_Graph.add(current.t, current.alt_m);
                    }
                    if (current.pressure_pa != null && options.contains("b_pres_depth")) {
                        baro_pres_depth_Graph.add(current.pressure_pa, current.alt_m);
                    }
                }
                // {"t":1731719001,"sensor":"hall_rpm","id":1,"rpm":1499}
                else if(line.contains("hall_rpm") && (options.contains("h_rpm"))){
                    Hall current = mapper.readValue(line, Hall.class);
                    if(current.id == 0 && options.contains("h_rpm")) {
                        hall_rmp_0_Graph.add(current.t, current.rpm);
                    }
                    if(current.id == 1 && options.contains("h_rpm")){
                        hall_rmp_1_Graph.add(current.t, current.rpm);
                    }
                }
            }
        }

        if (options.contains("t_temp")) {
            //int offset = 0, String title, String xLabel, String yLabel, XYSeries graphData
            graphDraw(offset, "Time vs Temperature", "Time", "Temperature", tempGraph, path);
            offset += OFFSET;
        }
        if (options.contains("t_hum")) {
            graphDraw(offset, "Time vs Humidity", "Time", "Humidity", humidGraph, path);
            offset += OFFSET;
        }

        if (options.contains("g_depth")) {
            graphDraw(offset, "Gyro: Time vs Depth", "Time", "Depth", gyro_depth_Graph, path);
            offset += OFFSET;
        }

        if (options.contains("g_pos")) {
            graphDraw(offset, "Gyro: Time vs Depth", "X Pos", "Y Pos", gyro_pos_Graph, path);
            offset += OFFSET;
        }

        if (options.contains("b_pres")) {
            graphDraw(offset, "Barometer: Time vs Pressure", "Time", "Pressure (PA)", baro_pres_Graph, path);
            offset += OFFSET;
        }

        if (options.contains("b_depth")) {
            graphDraw(offset, "Barometer: Time vs Depth", "Time", "Depth", baro_depth_Graph, path);
            offset += OFFSET;
        }

        if (options.contains("b_pres_depth")) {
            graphDraw(offset, "Barometer: Pressure vs Depth", "Pressure (PA)", "Depth", baro_pres_depth_Graph, path);
            offset += OFFSET;
        }

        if (options.contains("h_rpm")) {
            graphDrawDouble(offset, "Hall Sensor: Time vs RPM", "Time", "RPM", hall_rmp_0_Graph, hall_rmp_1_Graph, path);
            offset += OFFSET;
        }

        options(path, offset);

    }

    public static ArrayList<String> optionBox() throws IOException {

        JFrame frame = new JFrame("Graph options");
        frame.setResizable(false);
        frame.setSize(280, 540);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Checkbox opt1 = new Checkbox("Time vs temperature");
        opt1.setBounds(20, 40, 240, 20);
        frame.add(opt1);

        Checkbox opt2 = new Checkbox("Time vs humidity");
        opt2.setBounds(20, 70, 240, 20);
        frame.add(opt2);

        Checkbox opt3 = new Checkbox("Gyro [ time vs depth ]");
        opt3.setBounds(20, 100, 240, 20);
        frame.add(opt3);

        Checkbox opt4 = new Checkbox("Gyro [ x pos vs y pos ]");
        opt4.setBounds(20, 130, 240, 20);
        frame.add(opt4);

        Checkbox opt5 = new Checkbox("Barometer [ Time vs pressure ]");
        opt5.setBounds(20, 160, 240, 20);
        frame.add(opt5);

        Checkbox opt6 = new Checkbox("Barometer [ Time vs depth ]");
        opt6.setBounds(20, 190, 240, 20);
        frame.add(opt6);

        Checkbox opt7 = new Checkbox("Barometer [ Pressure vs depth ]");
        opt7.setBounds(20, 220, 240, 20);
        frame.add(opt7);

        Checkbox opt8 = new Checkbox("Hall effect [ Time vs RPM ]");
        opt8.setBounds(20, 250, 240, 20);
        frame.add(opt8);

        JButton button = new JButton("LOAD");
        button.setBounds(20, 300, 200, 20);
        frame.add(button);

        JLabel select = new JLabel("Select");
        select.setBounds(20, 330, 240, 20);
        frame.add(select);

        JButton selectAll = new JButton("All");
        selectAll.setBounds(20, 350, 100, 20);
        frame.add(selectAll);

        JButton selectNone = new JButton("None");
        selectNone.setBounds(120, 350, 100, 20);
        frame.add(selectNone);

        JButton temp_hum_sel = new JButton("Temp");
        temp_hum_sel.setBounds(20, 380, 70, 20);
        frame.add(temp_hum_sel);

        JButton gyro_sel = new JButton("Gyro");
        gyro_sel.setBounds(86, 380, 70, 20);
        frame.add(gyro_sel);

        JButton baro_sel = new JButton("Baro");
        baro_sel.setBounds(152, 380, 70, 20);
        frame.add(baro_sel);

        // insert logo image
        try {
            // Load the image from resources (logo.png in src/main/resources/)
            BufferedImage img = ImageIO.read(Main.class.getResource("/logo.png"));

            // Scale to fixed size, e.g., 100x50
            Image scaled = img.getScaledInstance(100, 50, Image.SCALE_SMOOTH);

            // Create JLabel with scaled image
            JLabel logoLabel = new JLabel(new ImageIcon(scaled));

            // Set absolute position
            logoLabel.setBounds(75, 420, 100, 50); // x=400, y=20, width=100, height=50

            // Make sure your contentPanel uses null layout
            frame.setLayout(null);

            // Add the image to the panel
            frame.add(logoLabel);

            // Refresh panel if already visible
            frame.revalidate();
            frame.repaint();

        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setVisible(true);

        boolean status = false;
        while (!status) {
            if (button.getModel().isPressed()) {
                status = true;
            }

            if(selectAll.getModel().isPressed()){
                opt1.setState(true);
                opt2.setState(true);
                opt3.setState(true);
                opt4.setState(true);
                opt5.setState(true);
                opt6.setState(true);
                opt7.setState(true);
                opt8.setState(true);
            } else if(selectNone.getModel().isPressed()){
                opt1.setState(false);
                opt2.setState(false);
                opt3.setState(false);
                opt4.setState(false);
                opt5.setState(false);
                opt6.setState(false);
                opt7.setState(false);
                opt8.setState(false);
            }
            else if(temp_hum_sel.getModel().isPressed()){
                opt1.setState(true);
                opt2.setState(true);
            }
            else if(gyro_sel.getModel().isPressed()){
                opt3.setState(true);
                opt4.setState(true);
            }
            else if(baro_sel.getModel().isPressed()){
                opt5.setState(true);
                opt6.setState(true);
                opt7.setState(true);
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> checkedBoxes = new ArrayList<>();

        if (opt1.getState()) {    // time vs temp
            checkedBoxes.add("t_temp");
        }
        if (opt2.getState()) {    // time vs humidity
            checkedBoxes.add("t_hum");
        }
        if (opt3.getState()) {    // gyro: t vs depth
            checkedBoxes.add("g_depth");
        }
        if (opt4.getState()) {    // gyro: x pos vs y pos
            checkedBoxes.add("g_pos");
        }
        if (opt5.getState()) {    // baro: t vs pres
            checkedBoxes.add("b_pres");
        }
        if (opt6.getState()) {    // baro: t vs depth
            checkedBoxes.add("b_depth");
        }
        if (opt7.getState()) {    // baro: pres vs depth
            checkedBoxes.add("b_pres_depth");
        }
        if (opt8.getState()) {    // hall effect: t vs rpm
            checkedBoxes.add("h_rpm");
        }

        frame.dispose();
        return checkedBoxes;
    }

    public static void graphDraw(int offset, String title, String xLabel, String yLabel, XYSeries graphData, String path){

        XYSeriesCollection opt = new XYSeriesCollection(graphData);
        JFreeChart opt_chart = ChartFactory.createXYLineChart(
                title,     // chart title
                xLabel,               // x-axis label
                yLabel,               // y-axis label
                opt                   // data
        );

        // Chart panels
        ChartPanel opt_pan = new ChartPanel(opt_chart);

        // Let charts resize horizontally
        opt_pan.setPreferredSize(new Dimension(320, 240));

        // Graph padding
        opt_pan.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content panel with layout manager
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Add charts (stacked vertically)
        contentPanel.add(opt_pan);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Frame
        JFrame frame = new JFrame(title + ": " + path);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation(10 + offset, 10 + offset);
        frame.add(scrollPane);
        frame.setSize(640, 480);
        frame.setVisible(true);
    }

    public static void graphDrawDouble(int offset, String title, String xLabel, String yLabel, XYSeries graphData1, XYSeries graphData2, String path) {
        // First dataset
        XYSeriesCollection dataset1 = new XYSeriesCollection(graphData1);
        JFreeChart chart1 = ChartFactory.createXYLineChart(
                "0 - " +title,
                xLabel,
                yLabel,
                dataset1
        );

        // Second dataset
        XYSeriesCollection dataset2 = new XYSeriesCollection(graphData2);
        JFreeChart chart2 = ChartFactory.createXYLineChart(
                "1 - " +title,
                xLabel,
                yLabel,
                dataset2
        );

        // Chart panels
        ChartPanel panel1 = new ChartPanel(chart1);
        ChartPanel panel2 = new ChartPanel(chart2);

        panel1.setPreferredSize(new Dimension(320, 240));
        panel2.setPreferredSize(new Dimension(320, 240));

        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content panel (vertical stack)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        contentPanel.add(panel1);
        contentPanel.add(panel2);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        // Frame
        JFrame frame = new JFrame(title + ": " + path);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocation(10 + offset, 10 + offset);
        frame.add(scrollPane);
        frame.setSize(640, 520);
        frame.setVisible(true);
    }

    public static String fileChooser(){
        /** file sector dialog **/
        String path = "";
        while(path.isEmpty()){
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "JSON or Text files", "json", "txt"
            );
            chooser.setFileFilter(filter);
            int result = chooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                path = chooser.getSelectedFile().getAbsolutePath();
                System.out.println("File: " + path + " selected.");
            }
            if (result == JFileChooser.CANCEL_OPTION) {
                System.out.println("Program terminated. No errors.");
                System.exit(0);
            }
        }
        return path;
    }

    public static void options(String path, int offset) throws IOException {
        JFrame frame = new JFrame("Options");
        frame.setResizable(false);
        frame.setSize(240, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = frame.getSize();

        int x = screen.width - size.width;
        int y = 0;
        frame.setLocation(x, y);
        frame.setVisible(true);

        frame.setLayout(null);

        JButton closeAll = new JButton("CLOSE ALL");
        closeAll.setBounds(20, 20, 200, 20);
        frame.add(closeAll);

        JButton exit = new JButton("EXIT PROGRAM");
        exit.setBounds(20, 50, 200, 20);
        frame.add(exit);

        JButton graph_opts = new JButton("GRAPH OPTIONS");
        graph_opts.setBounds(20, 80, 200, 20);
        frame.add(graph_opts);

        JButton file_chooser = new JButton("FILE CHOOSER");
        file_chooser.setBounds(20, 110, 200, 20);
        frame.add(file_chooser);

        Checkbox keep_grph_open = new Checkbox("Keep existing graphs open");
        keep_grph_open.setBounds(20, 140, 200, 20);
        frame.add(keep_grph_open);

        boolean status = false;
        while (!status) {
            if (closeAll.getModel().isPressed()) {
                offset=0;
                for (Frame f : Frame.getFrames()) {
                    f.dispose();
                    frame.setLocation(x, y);
                    frame.setVisible(true);
                }
            }
            else if (exit.getModel().isPressed()) {
                offset=0;
                System.exit(0);
            }
            else if (graph_opts.getModel().isPressed()) {
                for (Frame f : Frame.getFrames()) {
                    if(!keep_grph_open.getState()) {
                        f.dispose();
                    }
                    if (f.getTitle().equals("Options")) {
                        f.dispose();
                    }
                }
                if(!keep_grph_open.getState()){
                    offset=0;
                }
                String[] args = {path,String.valueOf(offset)};
                main(args);
            }
            else if (file_chooser.getModel().isPressed()) {
                for (Frame f : Frame.getFrames()) {
                    if(!keep_grph_open.getState()) {
                        f.dispose();
                    }
                    if (f.getTitle().equals("Options")) {
                        f.dispose();
                    }
                }
                if(!keep_grph_open.getState()){
                    offset=0;
                }
                String[] args = {"0", String.valueOf(offset)};
                main(args);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Worked");
        frame.dispose();
    }

}
