package edu.markc.bluetooth;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

public class Countdown extends CountDownTimer {
int speed;
int speedLimit;
Context ctx;
    public Countdown(long millisInFuture, long countDownInterval, int speedLimit, int speed) {
        super(millisInFuture, countDownInterval);
        this.speed = speed;
        this.speedLimit = speedLimit;
    }
    @Override
    public void onTick(long millisUntilFinished) {
        if (speed <= speedLimit)
        {
            this.cancel();
        }

    }

    @Override
    public void onFinish() {
      //  overthelimit(ctx);
    }


}
