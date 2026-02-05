package opensource.hexiano;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AboutDialog extends Dialog
{
    public AboutDialog(Context context)
    {
        super(context);
        
        String versionName;
        try
        {
        	PackageManager pm = context.getPackageManager();
        	String packageName = context.getPackageName();
        	PackageInfo pi = pm.getPackageInfo(packageName, 0);
            versionName = pi.versionName;
        } 
        catch (Exception ex)
        {
            versionName = "0.0a";
        }
        
        setContentView(R.layout.about);
        CharSequence name = context.getText(R.string.app_name);
        CharSequence versionStr = context.getText(R.string.version);
        StringBuilder title = new StringBuilder(name);
        title.append(" ").append(versionStr).append(" ").append(versionName);
        this.setTitle(title);
        
        // TextView licenseView = (TextView) findViewById(R.id.license);
        // CharSequence text = context.getText(R.string.license);
        // licenseView.setText(Html.fromHtml(text));
        // licenseView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
