package xyz.ariesfish.myprintservice;

import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintDocument;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyPrintService extends PrintService {
    @Nullable
    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        Log.d("MyPrinter", "MyPrintService#onCreatePrinterDiscoverySession() called");

        return new PrinterDiscoverySession() {
            @Override
            public void onStartPrinterDiscovery(@NonNull List<PrinterId> priorityList) {
                Log.d("MyPrinter", "PrinterDiscoverySession#onStartPrinterDiscovery(priorityList: )" + priorityList + ") called");

                if (!priorityList.isEmpty()) {
                    return;
                }

                final List<PrinterInfo> printers = new ArrayList<>();
                final PrinterId printerId = generatePrinterId("aaa");
                final PrinterInfo.Builder builder = new PrinterInfo.Builder(printerId, "dummy printer", PrinterInfo.STATUS_IDLE);
                PrinterCapabilitiesInfo.Builder capBuilder = new PrinterCapabilitiesInfo.Builder(printerId);
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
                        .addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                        .addResolution(new PrintAttributes.Resolution("resolutionId", "default resolution", 600, 600), true)
                        .setColorModes(PrintAttributes.COLOR_MODE_COLOR | PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_COLOR);
                builder.setCapabilities(capBuilder.build());
                printers.add(builder.build());
                addPrinters(printers);
            }

            @Override
            public void onStopPrinterDiscovery() {
                Log.d("MyPrinter", "PrinterDiscoverySession#onStopPrinterDiscovery() called");
            }

            @Override
            public void onValidatePrinters(@NonNull List<PrinterId> printerIds) {
                Log.d("MyPrinter", "PrinterDiscoverySession#onValidatePrinters(printerIds: )" + printerIds + ") called");
            }

            @Override
            public void onStartPrinterStateTracking(@NonNull PrinterId printerId) {
                Log.d("MyPrinter", "PrinterDiscoverySession#onStartPrinterStateTracking(printerId: )" + printerId + ") called");
            }

            @Override
            public void onStopPrinterStateTracking(@NonNull PrinterId printerId) {
                Log.d("MyPrinter", "PrinterDiscoverySession#onStopPrinterStateTracking(printerId: )" + printerId + ") called");
            }

            @Override
            public void onDestroy() {
                Log.d("MyPrinter", "PrinterDiscoverySession#onDestroy() called");
            }
        };
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.d("MyPrinter", "MyPrintService#Canceled: " + printJob.getId().toString());
        printJob.cancel();
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {
        Log.d("MyPrinter", "MyPrintService#Queued: " + printJob.getId().toString());

        printJob.start();

        final PrintDocument document = printJob.getDocument();
        final FileInputStream in = new FileInputStream(document.getData().getFileDescriptor());
        try {
            final byte[] buffer = new byte[4];
            @SuppressWarnings("unused")
            final int read = in.read(buffer);
            Log.d("MyPrinter", "First " + buffer.length + "bytes of content: " + buffer.toString());
        } catch (IOException e) {
            Log.d("MyPrinter", "", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                assert true;
            }
        }
        printJob.complete();
    }

    private static String toString(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Byte.toString(b)).append(',');
        }
        if (sb.length() != 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
