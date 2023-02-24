package edu.markc.bluetooth;

import android.app.Activity;
import android.content.Context;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import br.ufrn.imd.obd.commands.ObdCommandGroup;
import br.ufrn.imd.obd.commands.control.TroubleCodesCommand;
import br.ufrn.imd.obd.utils.TroubleCodeDescription;

public class OBDService {


    public static int getLiveRPM(InputStream finalInputStream, OutputStream finalOutputStream) {
        RPMCommand spd = new RPMCommand();
        try {
            spd.run(finalInputStream, finalOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = spd.getResult();
        String sub = result.substring(8);
        int rpm = Integer.parseInt(sub, 16)/4;

        return  rpm;
    }

    public static int getLiveSpeed(InputStream finalInputStream, OutputStream finalOutputStream) {
        SpeedCommand spd = new SpeedCommand();
        try {
            spd.run(finalInputStream, finalOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = spd.getResult();
        String sub = result.substring(8);
        return Integer.parseInt(sub, 16);
    }

    public static ArrayList<String> getfaults(InputStream finalInputStream, OutputStream finalOutputStream, Context main) {
        ObdCommandGroup commands = new ObdCommandGroup();

        commands.add(new TroubleCodesCommand());
        //commands.add(new TroubleCode);
        try {
            commands.run(finalInputStream, finalOutputStream);
            String r = commands.toString();


            ArrayList<String> dtcs = getDTCs(r, main);
            return  dtcs;

        }
        catch (IOException | InterruptedException e)
        {

        }
        return null;
    }
    private static ArrayList<String> getDTCs(String r, Context main) {
        ArrayList<String> dtcs = new ArrayList<>();
        TroubleCodeDescription troubleCodeDescription = TroubleCodeDescription.getInstance(main);

        String[] parts = r.split("\\[|,|\\]");
        //get index 1 up to Length - 1

        parts = Arrays.copyOfRange(parts, 1, parts.length-1);

        for (String d:parts) {
            //d is the dtc
            String dtc = d /*+ ": "*/;
            String t = troubleCodeDescription.getTroubleCodeDescription(d);

            dtc += ": "+ t;
            dtcs.add(dtc);
        }

        return dtcs;
    }


}
