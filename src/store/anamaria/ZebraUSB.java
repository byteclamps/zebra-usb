package store.anamaria;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.UsbConnection;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.DiscoveredUsbPrinter;
import com.zebra.sdk.printer.discovery.UsbDiscoverer;
import com.zebra.sdk.printer.discovery.ZebraPrinterFilter;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class ZebraUSB {
    private String address = "";

    public ZebraUSB () {}

    public static void main(String[] args) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Archivo csv - xls - xlsx");
        ZebraUSB zebraUSB = new ZebraUSB();
        String address = "";

        try {
            address = zebraUSB.findUSBPrinters().get(0);
        } catch (IndexOutOfBoundsException e) {
            System.exit(0);
        }

        new JFXPanel();

        String finalAddress = address;
        Platform.runLater(() -> {
            try {
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV, XLS, XLSX files (*.csv, *.xls, *.xlsx)", "*.csv", "*.xls", "*.xlsx");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showOpenDialog(null);
                String ext = FilenameUtils.getExtension(file.getName());

                switch (ext) {
                    case "csv":
                        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                            String line;
                            int counter = 0;

                            while ((line = br.readLine()) != null) {
                                String[] values = line.split(",");

                                if (counter > 0) {
                                    int to = Integer.parseInt(values[1]);

                                    for (int i = 0; i < to; i++) {
                                        zebraUSB.printBarcode(finalAddress, values[0], values[2]);
                                    }
                                }

                                counter++;
                            }
                        }
                        break;

                    case "xls":
                        break;

                    case "xlsx":
                        break;

                    default:
                        throw new Exception("El archivo es inválido.");
                }
            } catch (NullPointerException e) {
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            } finally {
                System.exit(1);
            }
        });
    }

    public List<String> findUSBPrinters () {
        List<String> printers = new ArrayList<>();

        try {
            for (DiscoveredUsbPrinter printer : UsbDiscoverer.getZebraUsbPrinters(new ZebraPrinterFilter())) {
                printers.add(printer.address);
            }
        } catch (ConnectionException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }

        return printers;
    }

    public boolean printBarcode(String address, String text, String price) {
        Connection printerConnection = null;

        if (address.length() == 0) {
            throw new RuntimeException("La dirección de la USB no puede estar vacía.");
        }

        if (text.length() == 0) {
            throw new RuntimeException("El código no puede estar vacío.");
        }

        setAddress(address);

        try {
            printerConnection = new UsbConnection(this.getAddress());
            printerConnection.open();

            ZebraPrinterFactory.getInstance(printerConnection).getPrinterControlLanguage();
            printerConnection.write(getBarcode(text, price));
            printerConnection.close();

            return true;
        } catch (ConnectionException e) {
            e.printStackTrace();

            return false;
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();

            return false;
        } finally {
            try {
                printerConnection.close();
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getBarcode(String text, String price) {
        int spacing = 170;

        if (text.length() > 12 || text.length() < 5)
            throw new RuntimeException("Código invalido.");

        if (text.length() == 12)
            spacing = 150;

        if (text.length() == 11)
            spacing = 155;

        if (text.length() == 10)
            spacing = 160;

        if (text.length() == 9)
            spacing = 165;

        if (text.length() == 8)
            spacing = 170;

        if (text.length() == 7)
            spacing = 175;

        if (text.length() == 6)
            spacing = 180;

        if (text.length() == 5)
            spacing = 185;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("^XA^FB400,2,2,C^CF0,20^FO40," + (spacing - 135) + "^FD" + price + "^FS^FO" + spacing + ",60^BY1.2^BC,90,Y,N,N,A^FD");

        if (text.length() == 0) {
            throw new RuntimeException("El código no puede estar vacío.");
        }

        stringBuilder.append(text);
        stringBuilder.append("^FS^XZ");

        return stringBuilder.toString().getBytes();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

