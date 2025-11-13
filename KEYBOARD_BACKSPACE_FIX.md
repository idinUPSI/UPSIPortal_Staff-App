# Keyboard Backspace Fix Documentation

## Problem Statement
Keyboard backspace/delete button tidak berfungsi dengan baik - perlu ditekan beberapa kali sebelum berfungsi.
(Keyboard backspace/delete button not working properly - requires multiple presses before functioning.)

## Root Cause
Beberapa keyboard Android (terutamanya Samsung, Huawei, dan keyboard pihak ketiga) memanggil method `deleteSurroundingText()` bukannya menghantar event `KEYCODE_DEL` yang betul. Ini menyebabkan backspace tidak berfungsi dengan baik dalam aplikasi berasaskan WebView.

(Some Android keyboards, especially Samsung, Huawei, and third-party keyboards, call the `deleteSurroundingText()` method instead of sending proper `KEYCODE_DEL` events. This causes backspace to not work properly in WebView-based applications.)

## Technical Details

### The Problem
1. Standard Android keyboards send `KeyEvent.KEYCODE_DEL` when backspace is pressed
2. Some manufacturers' keyboards call `InputConnection.deleteSurroundingText()` instead
3. WebView's default InputConnection doesn't properly handle these calls
4. Result: backspace appears to not work or requires multiple presses

### The Solution
The fix is implemented in `CustomCapacitorWebView.java`:

1. **Wrap, Don't Replace**: The custom InputConnection wraps the default WebView InputConnection instead of replacing it
2. **Intercept Delete Calls**: Intercepts `deleteSurroundingText()` and `deleteSurroundingTextInCodePoints()` 
3. **Convert to Key Events**: Converts these method calls to proper `KEYCODE_DEL` key events
4. **Delegate Everything Else**: All other input operations are delegated to the wrapped connection

### Changes Made

#### 1. CustomCapacitorWebView.java
- Simplified `dispatchKeyEvent()` to use default behavior
- Modified `onCreateInputConnection()` to wrap the default connection
- Rewrote `BackspaceFixInputConnection` as a wrapper instead of extending BaseInputConnection
- Removed unnecessary `BackspaceFixEditable` class and dummy buffer approach
- Added proper delegation for all InputConnection methods

#### 2. AndroidManifest.xml
- Changed `windowSoftInputMode` from `adjustPan` to `adjustResize`
- This provides better keyboard handling and layout adjustment

## Why This Approach Works

### Previous Implementation Issues
- Created a new `BaseInputConnection` which lost all default WebView input handling
- Used a dummy buffer approach that could interfere with normal text input
- Didn't properly preserve text composition and predictive text features

### Current Implementation Benefits
- Preserves all default WebView input handling behavior
- Only fixes the specific `deleteSurroundingText()` issue
- Works with all keyboard types and input methods
- Doesn't interfere with text composition, autocorrect, or predictive text
- More maintainable and less prone to edge case bugs

## Testing Instructions

1. Build and install the app on an Android device
2. Navigate to any page with text input fields
3. Test with different keyboards:
   - Default system keyboard (Gboard)
   - Samsung Keyboard (if available)
   - Huawei Keyboard (if available)
   - Other third-party keyboards (SwiftKey, etc.)
4. Verify:
   - Single press of backspace deletes one character
   - Holding backspace continuously deletes characters
   - Text composition/predictive text still works
   - Forward delete (if keyboard supports it) works

## References
- [Stack Overflow: Android cannot capture backspace/delete press in soft keyboard](https://stackoverflow.com/questions/18581636/android-cannot-capture-backspace-delete-press-in-soft-keyboard)
- [Android Issue Tracker: InputConnection deleteSurroundingText not working](https://issuetracker.google.com/issues/36922222)
- [Android Developers: InputConnection API](https://developer.android.com/reference/android/view/inputmethod/InputConnection)

## Files Modified
1. `android/app/src/main/java/my/edu/upsi/portal/staff/CustomCapacitorWebView.java`
2. `android/app/src/main/AndroidManifest.xml`
3. `.gitignore`

## Future Considerations
- Monitor for edge cases with specific keyboard apps
- Test on various Android versions (API 21+)
- Consider adding telemetry to track keyboard input issues
