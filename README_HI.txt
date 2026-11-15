BHAJAN FASHION POINT – Business Accounting Android Project
पहला वर्जन (MVP): Sale, Purchase, Cash Receipt, Cash Payment और Cash Summary

महत्वपूर्ण हिसाब नियम:
1) Sale Total में केवल SALE एंट्री जुड़ती है।
2) CASH_RECEIPT केवल Cash Balance बढ़ाता है, Sale Total नहीं।
3) CASH_PAYMENT केवल Cash Balance घटाता है, Sale Total नहीं।
4) Cash Sale से Cash बढ़ता है; Credit Sale से Cash नहीं बढ़ता।
5) Cash Purchase से Cash घटता है; Credit Purchase से Cash नहीं घटता।
6) डेटा फोन के SQLite database में रहता है।

बनाने का तरीका:
1) ZIP को extract करें।
2) Android Studio खोलें और Open चुनें।
3) Extract किए गए BFP_Business_Accounting_Android फ़ोल्डर को चुनें।
4) Gradle Sync पूरा होने दें।
5) फोन में Developer Options और USB Debugging चालू करके Run दबाएँ।
6) APK बनाने के लिए: Build > Build Bundle(s) / APK(s) > Build APK(s)

आवश्यकताएँ:
- Android Studio
- JDK 17
- Android SDK Platform 35
- इंटरनेट केवल पहली बार Gradle dependencies डाउनलोड करने के लिए आवश्यक हो सकता है।

नोट:
यह शुरुआती कार्यशील प्रोजेक्ट है, तैयार signed APK नहीं। अभी Customer/Supplier Ledger,
GST, Stock, Profit & Loss, Backup/Restore, edit-entry और opening cash सेट करने का UI
अगले चरणों में जोड़ा जाएगा। वास्तविक खातों में उपयोग से पहले अपने पुराने डेटा से मिलान
और परीक्षण अवश्य करें। Source code में बदलाव किए जा सकते हैं।
