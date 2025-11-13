package my.edu.upsi.portal.staff;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.getcapacitor.CapacitorWebView;

/**
 * Custom WebView with native backspace fix for Issue #62306.
 * 
 * This fixes the keyboard backspace issue where:
 * - Some keyboards (Samsung, Swype, etc) don't send proper KEYCODE_DEL events
 * - Letters require multiple backspace presses (buffer becomes empty)
 * - Numbers work fine (handled differently by keyboards)
 * 
 * Solution:
 * - Override onCreateInputConnection() to provide custom InputConnection
 * - Maintain dummy buffer so keyboard always thinks there's text to delete
 * - Convert deleteSurroundingText() calls to proper KEYCODE_DEL events
 * 
 * This is the standard Android solution documented on Stack Overflow:
 * https://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard
 */
public class CustomCapacitorWebView extends CapacitorWebView {
    
    public CustomCapacitorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // Get the default InputConnection from parent
        InputConnection defaultConnection = super.onCreateInputConnection(outAttrs);
        
        // Wrap it with our custom InputConnection that fixes backspace
        BackspaceFixInputConnection customConnection = new BackspaceFixInputConnection(
            this, 
            false, 
            outAttrs
        );
        
        return customConnection;
    }
    
    /**
     * Custom InputConnection that fixes backspace by:
     * 1. Maintaining a dummy buffer so keyboard doesn't think buffer is empty
     * 2. Converting deleteSurroundingText() to KEYCODE_DEL key events
     */
    private static class BackspaceFixInputConnection extends BaseInputConnection {
        private Editable mEditable;
        private static final String DUMMY_CHAR = "/";
        private EditorInfo mEditorInfo;
        
        public BackspaceFixInputConnection(CustomCapacitorWebView targetView, boolean fullEditor, EditorInfo outAttrs) {
            super(targetView, fullEditor);
            this.mEditorInfo = outAttrs;
        }
        
        @Override
        public Editable getEditable() {
            // Only apply fix on Android 4.0+ (API 14+)
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                if (mEditable == null) {
                    // Create editable with dummy character
                    mEditable = new BackspaceFixEditable(DUMMY_CHAR);
                    Selection.setSelection(mEditable, 1);
                } else if (mEditable.length() == 0) {
                    // Buffer became empty, refill with dummy character
                    mEditable.append(DUMMY_CHAR);
                    Selection.setSelection(mEditable, 1);
                }
                return mEditable;
            }
            return super.getEditable();
        }
        
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // This method is called by buggy keyboards instead of generating KEYCODE_DEL
            // We intercept it and generate proper key events instead
            if (android.os.Build.VERSION.SDK_INT >= 14 && beforeLength == 1 && afterLength == 0) {
                // Send proper backspace key down and up events
                boolean downSent = super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                boolean upSent = super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                return downSent && upSent;
            }
            // For other cases, use default behavior
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }
    
    /**
     * Custom Editable that maintains a dummy buffer to prevent keyboard from
     * stopping backspace generation when it thinks the buffer is empty.
     */
    private static class BackspaceFixEditable extends SpannableStringBuilder {
        private static final String DUMMY_CHAR = "/";
        
        BackspaceFixEditable(CharSequence source) {
            super(source);
        }
        
        @Override
        public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbstart, int tbend) {
            if (tbend > tbstart) {
                // Text is being inserted
                // Clear buffer and insert the new text
                super.replace(0, length(), "", 0, 0);
                return super.replace(0, 0, tb, tbstart, tbend);
            } else if (end > start) {
                // Text is being deleted
                // Clear buffer and insert dummy character to maintain buffer state
                super.replace(0, length(), "", 0, 0);
                return super.replace(0, 0, DUMMY_CHAR, 0, DUMMY_CHAR.length());
            }
            // No actual change, maintain current buffer state
            return super.replace(start, end, tb, tbstart, tbend);
        }
    }
}
