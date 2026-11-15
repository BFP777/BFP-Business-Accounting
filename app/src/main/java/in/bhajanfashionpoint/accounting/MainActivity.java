package in.bhajanfashionpoint.accounting;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private DB db;
    private LinearLayout root, summary, entries;
    private TextView saleValue, purchaseValue, cashValue, inValue, outValue;
    private EditText amountInput, partyInput, noteInput;
    private Spinner typeSpinner, modeSpinner;
    private String[] types = {"बिक्री (Sale)", "खरीद (Purchase)", "कैश प्राप्त (Cash Receipt)", "कैश भुगतान (Cash Payment)"};
    private String[] modes = {"Cash", "Credit"};
    private final String[] typeCodes = {"SALE","PURCHASE","CASH_RECEIPT","CASH_PAYMENT"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DB(this);
        buildUI();
        refresh();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density); }
    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(0xFF17231D); if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }
    private void pad(View v) { v.setPadding(dp(14),dp(10),dp(14),dp(10)); }
    private TextView heading(String s) {
        TextView t = text(s,18,true); t.setPadding(0,dp(14),0,dp(8)); return t;
    }
    private EditText edit(String hint, int inputType) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setTextSize(15);
        e.setHint(hint); e.setInputType(inputType); pad(e); return e;
    }
    private Button button(String s) {
        Button b = new Button(this); b.setText(s); b.setAllCaps(false); return b;
    }
    private void buildUI() {
        ScrollView scroll = new ScrollView(this);
        root = column(); root.setPadding(dp(16),dp(12),dp(16),dp(24));
        root.setBackgroundColor(0xFFF4F7F5);
        scroll.addView(root); setContentView(scroll);

        TextView brand = text("BHAJAN FASHION POINT",22,true);
        brand.setTextColor(0xFFFFFFFF); brand.setPadding(dp(16),dp(18),dp(16),dp(4));
        TextView sub = text("Business Accounting • Android App",13,false);
        sub.setTextColor(0xFFD8EADF); sub.setPadding(dp(16),0,dp(16),dp(16));
        LinearLayout banner = column(); banner.setBackgroundColor(0xFF123D2B);
        banner.addView(brand); banner.addView(sub); root.addView(banner);

        summary = column(); summary.setPadding(0,dp(12),0,dp(6)); root.addView(summary);
        root.addView(heading("नई एंट्री जोड़ें"));

        typeSpinner = new Spinner(this);
        typeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        root.addView(typeSpinner);
        amountInput = edit("राशि (₹)", 8194); root.addView(amountInput);
        partyInput = edit("पार्टी / व्यक्ति का नाम", 1);
        root.addView(partyInput);
        noteInput = edit("विवरण / नोट", 1); root.addView(noteInput);
        modeSpinner = new Spinner(this);
        modeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes));
        root.addView(text("बिक्री / खरीद का तरीका",13,true)); root.addView(modeSpinner);
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                modeSpinner.setVisibility((pos == 0 || pos == 1) ? View.VISIBLE : View.GONE);
            }
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        Button save = button("एंट्री सेव करें"); save.setTextColor(0xFFFFFFFF);
        save.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF176B45));
        save.setOnClickListener(v -> saveEntry()); root.addView(save);

        root.addView(heading("हाल की एंट्री"));
        entries = column(); root.addView(entries);
        TextView foot = text("ध्यान दें: Cash Receipt / Cash Payment का असर Cash Balance पर होगा, Sale Total पर नहीं।",12,false);
        foot.setTextColor(0xFF52645A); foot.setPadding(0,dp(18),0,0); root.addView(foot);
    }

    private void saveEntry() {
        String raw = amountInput.getText().toString().trim();
        if (raw.isEmpty()) { amountInput.setError("राशि भरें"); return; }
        double amount;
        try { amount = Double.parseDouble(raw); } catch(Exception e) { amount = 0; }
        if (amount <= 0) { amountInput.setError("सही राशि भरें"); return; }
        int pos = typeSpinner.getSelectedItemPosition();
        String type = typeCodes[pos];
        String mode = (pos <= 1 && modeSpinner.getSelectedItemPosition() == 1) ? "CREDIT" : "CASH";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        db.insert(date, type, amount, partyInput.getText().toString().trim(),
                noteInput.getText().toString().trim(), mode);
        amountInput.setText(""); partyInput.setText(""); noteInput.setText("");
        Toast.makeText(this, "एंट्री सेव हो गई", Toast.LENGTH_SHORT).show();
        refresh();
    }

    private String money(double n) {
        return "₹" + String.format(new Locale("en","IN"), "%,.2f", n);
    }
    private void refresh() {
        double sale = db.sum("SALE");
        double purchase = db.sum("PURCHASE");
        double cashIn = db.cashIn();
        double cashOut = db.cashOut();
        double cash = db.openingCash() + cashIn - cashOut;

        summary.removeAllViews();
        summary.addView(heading("आज तक का सारांश"));
        addStat("कुल बिक्री (केवल Sale Entry)", money(sale), 0xFF176B45);
        addStat("कुल खरीद (Purchase)", money(purchase), 0xFF245A9A);
        addStat("Cash प्राप्त", money(cashIn), 0xFF176B45);
        addStat("Cash भुगतान", money(cashOut), 0xFFB33A32);
        addStat("वर्तमान Cash Balance", money(cash), 0xFF123D2B);

        entries.removeAllViews();
        Cursor c = db.list();
        if (c.getCount() == 0) {
            TextView empty = text("अभी कोई एंट्री नहीं है। ऊपर से पहली एंट्री जोड़ें।",14,false);
            empty.setPadding(0,dp(8),0,dp(8)); entries.addView(empty);
        }
        while (c.moveToNext()) {
            long id = c.getLong(0); String date = c.getString(1), type = c.getString(2);
            double amount = c.getDouble(3); String party = c.getString(4);
            String note = c.getString(5); String mode = c.getString(6);
            LinearLayout card = column(); card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(12),dp(8),dp(12),dp(8));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2); cp.bottomMargin = dp(8);
            entries.addView(card,cp);
            String label = typeLabel(type);
            TextView top = text(label + "   •   " + date,14,true); card.addView(top);
            TextView mid = text(money(amount) + "   •   " + (party.isEmpty()?"बिना नाम":party),16,true);
            mid.setTextColor(type.equals("CASH_PAYMENT") ? 0xFFB33A32 : 0xFF17231D); card.addView(mid);
            TextView detail = text((type.equals("SALE") || type.equals("PURCHASE") ? (mode.equals("CREDIT")?"उधार":"Cash") + " • " : "") + note,12,false);
            detail.setTextColor(0xFF66756D); card.addView(detail);
            Button del = button("यह एंट्री हटाएँ"); del.setTextSize(12);
            del.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("एंट्री हटाएँ?")
                .setMessage("क्या आप यह एंट्री हटाना चाहते हैं?")
                .setNegativeButton("नहीं",null).setPositiveButton("हाँ",(d,w)->{db.delete(id);refresh();}).show());
            card.addView(del);
        }
        c.close();
    }
    private void addStat(String label, String value, int color) {
        LinearLayout r = row(); r.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(6);
        summary.addView(r,p);
        TextView l = text(label,13,false); TextView v = text(value,15,true); v.setTextColor(color);
        r.addView(l,new LinearLayout.LayoutParams(0,-2,1)); r.addView(v);
        r.setPadding(dp(12),dp(12),dp(12),dp(12));
    }
    private String typeLabel(String t) {
        switch(t) {
            case "SALE": return "बिक्री";
            case "PURCHASE": return "खरीद";
            case "CASH_RECEIPT": return "कैश प्राप्त";
            default: return "कैश भुगतान";
        }
    }

    static class DB extends SQLiteOpenHelper {
        DB(Context c) { super(c,"bfp_accounting.db",null,1); }
        public void onCreate(SQLiteDatabase d) {
            d.execSQL("CREATE TABLE entries(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT NOT NULL, type TEXT NOT NULL, amount REAL NOT NULL, party TEXT, note TEXT, mode TEXT NOT NULL)");
            d.execSQL("CREATE TABLE settings(k TEXT PRIMARY KEY, v REAL NOT NULL)");
            d.execSQL("INSERT INTO settings(k,v) VALUES('opening_cash',0)");
        }
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV) {}
        void insert(String date,String type,double amount,String party,String note,String mode) {
            ContentValues v=new ContentValues(); v.put("date",date);v.put("type",type);v.put("amount",amount);
            v.put("party",party);v.put("note",note);v.put("mode",mode);
            getWritableDatabase().insert("entries",null,v);
        }
        double sum(String type) {
            Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM entries WHERE type=?",new String[]{type});
            double n=0;if(c.moveToFirst())n=c.getDouble(0);c.close();return n;
        }
        double cashIn() {
            Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM entries WHERE (type='SALE' AND mode='CASH') OR type='CASH_RECEIPT'",null);
            double n=0;if(c.moveToFirst())n=c.getDouble(0);c.close();return n;
        }
        double cashOut() {
            Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM entries WHERE (type='PURCHASE' AND mode='CASH') OR type='CASH_PAYMENT'",null);
            double n=0;if(c.moveToFirst())n=c.getDouble(0);c.close();return n;
        }
        double openingCash() {
            Cursor c=getReadableDatabase().rawQuery("SELECT v FROM settings WHERE k='opening_cash'",null);
            double n=0;if(c.moveToFirst())n=c.getDouble(0);c.close();return n;
        }
        Cursor list() { return getReadableDatabase().rawQuery("SELECT id,date,type,amount,COALESCE(party,''),COALESCE(note,''),mode FROM entries ORDER BY id DESC",null); }
        void delete(long id) { getWritableDatabase().delete("entries","id=?",new String[]{String.valueOf(id)}); }
    }
}
