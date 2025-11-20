package my.edu.upsi.portal.staff;

import android.content.Context;
import android.util.AttributeSet;

import com.getcapacitor.CapacitorWebView;

/**
 * Custom WebView that extends CapacitorWebView to use native Android keyboard.
 * 
 * This class simply extends the standard CapacitorWebView without any custom
 * keyboard handling, allowing Android's native keyboard input to work naturally
 * with the WebView. This provides the best compatibility with various keyboards
 * and input methods.
 */
public class CustomCapacitorWebView extends CapacitorWebView {
    
    public CustomCapacitorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
