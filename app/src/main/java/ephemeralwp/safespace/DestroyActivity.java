package ephemeralwp.safespace;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;

public class DestroyActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();		
		wipe.wipe(this);	
	}	
					
}
