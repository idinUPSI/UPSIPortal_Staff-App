package my.edu.upsi.portal.staff;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.getcapacitor.CapacitorWebView;

/**
 * Custom WebView with native keyboard fixes for backspace/delete key issue.
 * 
 * This fixes the keyboard issue where backspace/delete button doesn't work properly
 * and requires multiple presses before functioning.
 * 
 * The Problem:
 * Some Android keyboards (especially Samsung, Huawei, and some third-party keyboards)
 * call deleteSurroundingText() instead of sending KEYCODE_DEL events.
 * This causes backspace to not work in WebView-based applications.
 * 
 * The Solution:
 * - Wraps the default WebView InputConnection (instead of replacing it)
 * - Intercepts deleteSurroundingText() calls
 * - Converts them to proper KEYCODE_DEL/KEYCODE_FORWARD_DEL key events
 * - Delegates all other input operations to the original connection
 * 
 * This approach is more robust than using a dummy buffer because it:
 * 1. Preserves all default WebView input handling behavior
 * 2. Only fixes the specific backspace issue
 * 3. Works with all keyboard types and input methods
 * 4. Doesn't interfere with text composition, autocorrect, or predictive text
 * 
 * References:
 * - https://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard
 * - https://issuetracker.google.com/issues/36922222
 */
public class CustomCapacitorWebView extends CapacitorWebView {
    
    public CustomCapacitorWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Ensure all keyboard events are properly dispatched to the WebView
        // This fixes issues where some keys might be intercepted by the system
        
        // Let WebView handle the event - just use default behavior
        // The InputConnection fix below handles backspace properly
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // Get the default InputConnection from parent WebView
        InputConnection defaultConnection = super.onCreateInputConnection(outAttrs);
        
        if (defaultConnection == null) {
            return null;
        }
        
        // Wrap it with our custom InputConnection that fixes backspace
        // This delegates most operations to the default connection but fixes
        // the deleteSurroundingText issue that causes backspace problems
        return new BackspaceFixInputConnection(
            defaultConnection,
            this, 
            false
        );
    }
    
    /**
     * Custom InputConnection that fixes backspace by:
     * 1. Wrapping the default WebView InputConnection
     * 2. Intercepting deleteSurroundingText() calls that buggy keyboards use
     * 3. Converting them to proper KEYCODE_DEL key events
     * 4. Delegating all other operations to the wrapped connection
     */
    private static class BackspaceFixInputConnection implements InputConnection {
        private final InputConnection mTarget;
        private final CustomCapacitorWebView mView;
        
        public BackspaceFixInputConnection(InputConnection target, CustomCapacitorWebView targetView, boolean fullEditor) {
            this.mTarget = target;
            this.mView = targetView;
        }
        
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            // This method is called by buggy keyboards instead of generating KEYCODE_DEL
            // We intercept it and generate proper key events instead
            if (beforeLength > 0 && afterLength == 0) {
                // Send proper backspace key down and up events for each character to delete
                boolean success = true;
                for (int i = 0; i < beforeLength; i++) {
                    success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                }
                return success;
            } else if (beforeLength == 0 && afterLength > 0) {
                // Delete after cursor (forward delete)
                boolean success = true;
                for (int i = 0; i < afterLength; i++) {
                    success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_FORWARD_DEL));
                    success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_FORWARD_DEL));
                }
                return success;
            }
            // For other cases, delegate to the wrapped connection
            return mTarget.deleteSurroundingText(beforeLength, afterLength);
        }
        
        // Delegate all other methods to the wrapped connection
        
        @Override
        public CharSequence getTextBeforeCursor(int n, int flags) {
            return mTarget.getTextBeforeCursor(n, flags);
        }
        
        @Override
        public CharSequence getTextAfterCursor(int n, int flags) {
            return mTarget.getTextAfterCursor(n, flags);
        }
        
        @Override
        public CharSequence getSelectedText(int flags) {
            return mTarget.getSelectedText(flags);
        }
        
        @Override
        public int getCursorCapsMode(int reqModes) {
            return mTarget.getCursorCapsMode(reqModes);
        }
        
        @Override
        public android.view.inputmethod.ExtractedText getExtractedText(android.view.inputmethod.ExtractedTextRequest request, int flags) {
            return mTarget.getExtractedText(request, flags);
        }
        
        @Override
        public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
            // Similar fix for code points
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                if (beforeLength > 0 && afterLength == 0) {
                    boolean success = true;
                    for (int i = 0; i < beforeLength; i++) {
                        success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                        success = success && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                    }
                    return success;
                }
                return mTarget.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
            }
            return false;
        }
        
        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return mTarget.setComposingText(text, newCursorPosition);
        }
        
        @Override
        public boolean setComposingRegion(int start, int end) {
            return mTarget.setComposingRegion(start, end);
        }
        
        @Override
        public boolean finishComposingText() {
            return mTarget.finishComposingText();
        }
        
        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return mTarget.commitText(text, newCursorPosition);
        }
        
        @Override
        public boolean commitCompletion(android.view.inputmethod.CompletionInfo text) {
            return mTarget.commitCompletion(text);
        }
        
        @Override
        public boolean commitCorrection(android.view.inputmethod.CorrectionInfo correctionInfo) {
            return mTarget.commitCorrection(correctionInfo);
        }
        
        @Override
        public boolean setSelection(int start, int end) {
            return mTarget.setSelection(start, end);
        }
        
        @Override
        public boolean performEditorAction(int editorAction) {
            return mTarget.performEditorAction(editorAction);
        }
        
        @Override
        public boolean performContextMenuAction(int id) {
            return mTarget.performContextMenuAction(id);
        }
        
        @Override
        public boolean beginBatchEdit() {
            return mTarget.beginBatchEdit();
        }
        
        @Override
        public boolean endBatchEdit() {
            return mTarget.endBatchEdit();
        }
        
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            // Send key events directly to the WebView
            return mView.dispatchKeyEvent(event);
        }
        
        @Override
        public boolean clearMetaKeyStates(int states) {
            return mTarget.clearMetaKeyStates(states);
        }
        
        @Override
        public boolean reportFullscreenMode(boolean enabled) {
            return mTarget.reportFullscreenMode(enabled);
        }
        
        @Override
        public boolean performPrivateCommand(String action, android.os.Bundle data) {
            return mTarget.performPrivateCommand(action, data);
        }
        
        @Override
        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return mTarget.requestCursorUpdates(cursorUpdateMode);
        }
        
        @Override
        public android.os.Handler getHandler() {
            return mTarget.getHandler();
        }
        
        @Override
        public void closeConnection() {
            mTarget.closeConnection();
        }
        
        @Override
        public boolean commitContent(android.view.inputmethod.InputContentInfo inputContentInfo, int flags, android.os.Bundle opts) {
            return mTarget.commitContent(inputContentInfo, flags, opts);
        }
    }
    
    /**
     * REMOVED: BackspaceFixEditable class - no longer needed with delegation approach
     */
}
