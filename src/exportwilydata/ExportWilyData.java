package exportwilydata;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pool3
 */
public class ExportWilyData {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Connection con = null;

        ArrayList<String> metricas = new ArrayList();
        metricas.add("CPU:Utilization");
        metricas.add("Memory:FreeMemory.*KB");
        metricas.add("Memory:FreeSwap.*KB");
        metricas.add("Memory:Page.*Out.*KB");
        
//        metricas.add("Extended.*Memory.*Current.*used.*KB");
//        metricas.add("Heap.*Memory.*Current.*used.*KB");
//        metricas.add("BTC.*Running.*");
//        metricas.add("DIA.*Running.*");
        
        ArrayList<String> fileName = new ArrayList();
        fileName.add("CPU_Utilization");
        fileName.add("FreeMemory");
        fileName.add("FreeSwap");
        fileName.add("Page_Out");
        
//        fileName.add("Extended_Memory");
//        fileName.add("Heap_Memory");
//        fileName.add("BTC");
//        fileName.add("DIA");
        
//        String hosts="hcbiwp";
//        String prefijo="BW_";
        
        String hosts="hc.*p00";
        String prefijo="blades_";
        
        try {
            Class.forName("com.wily.introscope.jdbc.IntroscopeDriver");
            con = DriverManager.getConnection("jdbc:introscope:net//Admin:Admin89@hcsold00:6001", "", "");

            String agentRegex = ".*"+hosts+".*SAP HostAgent SMDA9.*";
            String metricRegex = ".*CPU:Utilization.*";
            String startTime = "06/26/13 11:00:00";
            String stopTime = "06/27/13 11:00:00";
            String host = ".*hcbiwp00.*";

            System.out.println("Calling getMetrics");

            ResultSet rs;

            for (int item = 0; item < metricas.size(); item++) {



                rs = getMetrics(con, host, agentRegex, metricas.get(item), startTime, stopTime);

                rs.last();
                System.out.println("Number of rows: " + rs.getRow());
                rs.beforeFirst();

                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();

                System.out.println("ColumnCount: " + columnCount);
                try {
                    File f;
                    //String nameFile = fileName.get(item)+".csv";
                    f = new File(prefijo+fileName.get(item)+".csv");


                    if (f.exists()) {
                        f.delete();
                    }

                    //Escritura

                    FileWriter w = new FileWriter(f);
                    BufferedWriter bw = new BufferedWriter(w);
                    PrintWriter wr = new PrintWriter(bw);

                    //Encabezados
                    for (int i = 1; i <= columnCount; i++) {
                        if (i == columnCount) {
                            wr.append(rsmd.getColumnName(i));
                        } else {
                            wr.append(rsmd.getColumnName(i) + ",");
                        }
                    }
                    wr.append("\n");

                    //Datos
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            if (i == columnCount) {
                                wr.append(rs.getString(i));
                            } else {
                                wr.append(rs.getString(i) + ",");
                            }
                        }
                        wr.append("\n");
                    }
                    wr.close();
                    bw.close();

                } catch (IOException ex) {
                    Logger.getLogger(ExportWilyData.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Couldn't load driver: " + cnfe.getMessage());
        } catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public static ResultSet getMetrics(Connection con, String host, String agentRegex, String metricRegex, String startTime, String stopTime) {
        //String selectStatement = "select * from metric_data where agent='" + agentRegex + "' and metric='" + metricRegex + "' and timestamp between '" + startTime + "' and '" + stopTime + "' sort by value d maxmatches=20 period=900";
        String selectStatement = "select * from metric_data where agent='" + agentRegex + "' and metric='.*" + metricRegex + ".*' and timestamp between '" + startTime + "' and '" + stopTime + "' period=1440";
        System.out.println("Statement: " + selectStatement);

        ResultSet rs = null;
        try {
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery(selectStatement);
        } catch (SQLException sqle) {
            System.out.println("SQLException: " + sqle.getMessage());
        }
        return rs;
    }
}
