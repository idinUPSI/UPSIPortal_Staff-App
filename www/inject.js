// CSS Injection for UPSI Portal Mobile Optimization
// Fix status bar overlap issue
(function() {
    'use strict';
    
    function injectMobileCSS() {
        // Check if already injected
        if (document.getElementById('upsi-mobile-css')) return;
        
        // Jangan inject untuk splash screen (index.html)
        // Hanya inject untuk portal content (unistaff.upsi.edu.my)
        const currentUrl = window.location.href;
        const isSplashScreen = currentUrl.includes('index.html') || 
                              currentUrl.endsWith('/') || 
                              !currentUrl.includes('unistaff.upsi.edu.my');
        
        if (isSplashScreen) {
            // Untuk splash screen, jangan inject CSS yang akan affect logo position
            return;
        }
        
        const style = document.createElement('style');
        style.id = 'upsi-mobile-css';
        style.textContent = `
            /* PWA Safe Area Handling - Fix Status Bar Overlap (Portal Content Sahaja) */
            :root {
                --safe-area-inset-top: env(safe-area-inset-top, 39px);
                --safe-area-inset-bottom: env(safe-area-inset-bottom, 0px);
                --safe-area-inset-left: env(safe-area-inset-left, 0px);
                --safe-area-inset-right: env(safe-area-inset-right, 0px);
            }
            
            /* Fix untuk html element - hanya untuk portal */
            html {
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }
            
            /* Fix untuk body - prevent overlap dengan status bar */
            body {
                margin-top: 0 !important;
                padding-top: var(--safe-area-inset-top) !important;
                padding-bottom: var(--safe-area-inset-bottom) !important;
                padding-left: var(--safe-area-inset-left) !important;
                padding-right: var(--safe-area-inset-right) !important;
            }
            
            /* Fix untuk header elements - adjust untuk elak terlalu tinggi */
            .page-header, header, .header, [role="banner"], 
            .navbar, .top-bar, .site-header, .main-header,
            [class*="header"], [class*="Header"], [id*="header"], [id*="Header"],
            nav[class*="navbar"], nav[class*="Navbar"] {
                position: relative !important;
                top: 0 !important;
                margin-top: 0 !important;
                /* Kurangkan padding untuk elak terlalu tinggi - hanya safe area sahaja */
                padding-top: var(--safe-area-inset-top) !important;
                min-height: auto !important;
                height: auto !important;
                z-index: 1000 !important;
            }
            
            /* Fix untuk white header bar yang terlalu tinggi */
            .navbar, .top-bar, [class*="navbar"], [class*="top-bar"] {
                padding-top: calc(var(--safe-area-inset-top) * 0.5) !important;
                padding-bottom: 0.5rem !important;
            }
            
            /* Fix untuk fixed/sticky headers */
            header[style*="fixed"], header[style*="sticky"],
            .header[style*="fixed"], .header[style*="sticky"],
            .navbar[style*="fixed"], .navbar[style*="sticky"] {
                top: var(--safe-area-inset-top) !important;
                padding-top: 0 !important;
            }
            
            /* Fix untuk container elements - kurangkan padding */
            .container, .container-fluid, main, .main-content, .content, #content {
                padding-top: var(--safe-area-inset-top) !important;
                padding-left: max(1rem, var(--safe-area-inset-left)) !important;
                padding-right: max(1rem, var(--safe-area-inset-right)) !important;
                margin-top: 0 !important;
            }
            
            /* Fix untuk fixed/sticky elements */
            [style*="position: fixed"], [style*="position: sticky"] {
                top: var(--safe-area-inset-top) !important;
            }
            
            /* Fix untuk iframe/webview content */
            iframe, webview {
                margin-top: var(--safe-area-inset-top) !important;
            }
        `;
        
        document.head.appendChild(style);
        console.log('UPSI Mobile CSS injected - Status bar overlap fixed (Portal only)');
    }
    
    // Inject immediately if DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', injectMobileCSS);
    } else {
        injectMobileCSS();
    }
    
    // Also inject after delays for dynamic content (portal loads dynamically)
    setTimeout(injectMobileCSS, 500);
    setTimeout(injectMobileCSS, 1000);
    setTimeout(injectMobileCSS, 3000);
    
    // Re-inject when page visibility changes (portal navigation)
    document.addEventListener('visibilitychange', () => {
        if (!document.getElementById('upsi-mobile-css')) {
            injectMobileCSS();
        }
    });
})();