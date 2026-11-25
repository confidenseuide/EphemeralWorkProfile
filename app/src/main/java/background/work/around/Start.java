package background.work.around;

import android.content.*;

public class Start {
    public static void RunService(Context context) {
        try{                  
			Intent intent = new Intent(context, StartReceiver.class);
			context.sendBroadcast(intent);
        } catch(Throwable t) {}
		try{        
			Intent intent = new Intent(context, ephemeralwp.safespace.WatcherService.class);        
			context.startForegroundService(intent);
			intent = new Intent(context, RiderService.class);
			context.startForegroundService(intent);            
        } catch(Throwable t) {}
    }
}
