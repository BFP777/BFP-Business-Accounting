
package in.bhajanfashionpoint.accounting;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.graphics.Typeface;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {

    private DB db;
    private LinearLayout root, summary, entries, itemRows;
    private LinearLayout itemSection;
    private TextView saleTotal;
    private EditText amountInput, partyInput, noteInput;
    private Spinner typeSpinner, modeSpinner;

    private final String[] types = {
        "बिक्री (Sale)",
        "खरीद (Purchase)",
        "कैश प्राप्त (Cash Receipt)",
        "कैश भुगतान (Cash Payment)"
    };

    private final String[] modes = {"Cash", "Credit"};
    private final String[] typeCodes = {
        "SALE", "PURCHASE", "CASH_RECEIPT", "CASH_PAYMENT"
    };

    private final ArrayList<ItemRow> itemList = new ArrayList<>();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new DB(this);
        buildUI();
        refresh();
    }

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    private TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(0xFF17231D);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        return t;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private void pad(View v) {
        v.setPadding(dp(14), dp(10), dp(14), dp(10));
    }

    private TextView heading(String s) {
        TextView t = text(s, 18, true);
        t.setPadding(0, dp(14), 0, dp(8));
        return t;
    }

    private EditText edit(String hint, int inputType) {
        EditText e = new EditText(this);
        e.setSingleLine(true);
        e.setTextSize(15);
        e.setHint(hint);
        e.setInputType(inputType);
        pad(e);
        return e;
    }

    private Button button(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        return b;
    }

    private void buildUI() {

        ScrollView scroll = new ScrollView(this);
        root = column();
        root.setPadding(dp(16), dp(12), dp(16), dp(24));
        root.setBackgroundColor(0xFFF4F7F5);
        scroll.addView(root);
        setContentView(scroll);

        TextView brand = text("BHAJAN FASHION POINT", 22, true);
        brand.setTextColor(0xFFFFFFFF);
        brand.setPadding(dp(16), dp(18), dp(16), dp(4));

        TextView sub = text("Business Accounting • Android App", 13, false);
        sub.setTextColor(0xFFD8EADF);
        sub.setPadding(dp(16), 0, dp(16), dp(16));

        LinearLayout banner = column();
        banner.setBackgroundColor(0xFF123D2B);
        banner.addView(brand);
        banner.addView(sub);
        root.addView(banner);

        summary = column();
        summary.setPadding(0, dp(12), 0, dp(6));
        root.addView(summary);

        root.addView(heading("नई एंट्री जोड़ें"));

        typeSpinner = new Spinner(this);
        typeSpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        ));
        root.addView(typeSpinner);

        // सामान्य राशि: खरीद और कैश एंट्री के लिए
        amountInput = edit("राशि (₹)", 8194);
        root.addView(amountInput);

        // बिक्री के आइटम का सेक्शन
        itemSection = column();
        itemSection.setVisibility(View.GONE);
        root.addView(itemSection);

        itemSection.addView(heading("बिक्री के आइटम"));

        itemRows = column();
        itemSection.addView(itemRows);

        Button addItem = button("+ दूसरा आइटम जोड़ें");
        addItem.setOnClickListener(v -> addItemRow());
        itemSection.addView(addItem);

        saleTotal = text("कुल बिल: ₹0.00", 18, true);
        saleTotal.setTextColor(0xFF176B45);
        saleTotal.setPadding(0, dp(12), 0, dp(12));
        itemSection.addView(saleTotal);

        // ग्राहक / पार्टी
        partyInput = edit("ग्राहक / पार्टी का नाम", 1);
        root.addView(partyInput);

        noteInput = edit("विवरण / नोट", 1);
        root.addView(noteInput);

        modeSpinner = new Spinner(this);
        modeSpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            modes
        ));

        root.addView(text("बिक्री / खरीद का तरीका", 13, true));
        root.addView(modeSpinner);

        typeSpinner.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {
                public void onItemSelected(
                    android.widget.AdapterView<?> p,
                    View v,
                    int pos,
                    long id
                ) {
                    updateEntryFields(pos);
                }

                public void onNothingSelected(
                    android.widget.AdapterView<?> p
                ) {}
            }
        );

        Button save = button("एंट्री सेव करें");
        save.setTextColor(0xFFFFFFFF);
        save.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(0xFF176B45)
        );
        save.setOnClickListener(v -> saveEntry());
        root.addView(save);

        root.addView(heading("हाल की एंट्री"));
        entries = column();
        root.addView(entries);

        TextView foot = text(
            "ध्यान दें: Cash Receipt / Cash Payment का असर Cash Balance पर होगा, Sale Total पर नहीं।",
            12,
            false
        );
        foot.setTextColor(0xFF52645A);
        foot.setPadding(0, dp(18), 0, 0);
        root.addView(foot);
    }

    private void updateEntryFields(int pos) {
        boolean isSale = (pos == 0);
        boolean isPurchase = (pos == 1);

        amountInput.setVisibility(isSale ? View.GONE : View.VISIBLE);
        itemSection.setVisibility(isSale ? View.VISIBLE : View.GONE);

        modeSpinner.setVisibility(
            (isSale || isPurchase) ? View.VISIBLE : View.GONE
        );

        if (isSale && itemList.isEmpty()) {
            addItemRow();
        }
    }

    // एक नया आइटम जोड़ना
    private void addItemRow() {

        LinearLayout card = column();
        card.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams cp =
            new LinearLayout.LayoutParams(-1, -2);
        cp.bottomMargin = dp(8);

        itemRows.addView(card, cp);

        EditText name = edit("आइटम का नाम", 1);
        card.addView(name);

        LinearLayout fields = row();

        EditText qty = edit("मात्रा", 8194);
        EditText rate = edit("रेट ₹", 8194);

        fields.addView(qty, new LinearLayout.LayoutParams(0, -2, 1));
        fields.addView(rate, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(fields);

        LinearLayout bottom = row();

        TextView total = text("रकम: ₹0.00", 14, true);
        total.setTextColor(0xFF176B45);

        Button remove = button("हटाएँ");
        remove.setTextSize(12);

        bottom.addView(total, new LinearLayout.LayoutParams(0, -2, 1));
        bottom.addView(remove);

        card.addView(bottom);

        ItemRow item = new ItemRow(name, qty, rate, total, card);
        itemList.add(item);

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(
                CharSequence s, int start, int count, int after
            ) {}

            public void onTextChanged(
                CharSequence s, int start, int before, int count
            ) {
                updateSaleTotal();
            }

            public void afterTextChanged(Editable e) {}
        };

        name.addTextChangedListener(watcher);
        qty.addTextChangedListener(watcher);
        rate.addTextChangedListener(watcher);

        remove.setOnClickListener(v -> {
            if (itemList.size() <= 1) {
                Toast.makeText(
                    this,
                    "कम से कम एक आइटम रखें",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            itemRows.removeView(card);
            itemList.remove(item);
            updateSaleTotal();
        });
    }

    private double number(EditText e) {
        try {
            String s = e.getText().toString().trim();
            if (s.isEmpty()) return 0;
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return 0;
        }
    }

    private double rowTotal(ItemRow item) {
        return number(item.qty) * number(item.rate);
    }

    private void updateSaleTotal() {
        double total = 0;

        for (ItemRow item : itemList) {
            double amount = rowTotal(item);
            total += amount;
            item.total.setText("रकम: " + money(amount));
        }

        saleTotal.setText("कुल बिल: " + money(total));
    }

    private void saveEntry() {

        int pos = typeSpinner.getSelectedItemPosition();
        String type = typeCodes[pos];

        String party = partyInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();

        String mode =
            (pos <= 1 && modeSpinner.getSelectedItemPosition() == 1)
            ? "CREDIT" : "CASH";

        String date = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.US
        ).format(new Date());

        double amount = 0;

        if (type.equals("SALE")) {

            ArrayList<SaleItem> items = new ArrayList<>();

            for (ItemRow r : itemList) {

                String name = r.name.getText().toString().trim();
                String qtyText = r.qty.getText().toString().trim();
                String rateText = r.rate.getText().toString().trim();

                if (name.isEmpty() && qtyText.isEmpty() && rateText.isEmpty()) {
                    continue;
                }

                if (name.isEmpty()) {
                    r.name.setError("आइटम का नाम भरें");
                    return;
                }

                double qty = number(r.qty);
                double rate = number(r.rate);

                if (qty <= 0) {
                    r.qty.setError("सही मात्रा भरें");
                    return;
                }

                if (rate <= 0) {
                    r.rate.setError("सही रेट भरें");
                    return;
                }

                double lineTotal = qty * rate;
                amount += lineTotal;

                items.add(new SaleItem(name, qty, rate, lineTotal));
            }

            if (items.isEmpty() || amount <= 0) {
                Toast.makeText(
                    this,
                    "बिक्री का कम से कम एक आइटम भरें",
                    Toast.LENGTH_SHORT
                ).show();
                return;
            }

            db.insertSale(date, amount, party, note, mode, items);

        } else {

            String raw = amountInput.getText().toString().trim();

            if (raw.isEmpty()) {
                amountInput.setError("राशि भरें");
                return;
            }

            try {
                amount = Double.parseDouble(raw);
            } catch (Exception e) {
                amount = 0;
            }

            if (amount <= 0) {
                amountInput.setError("सही राशि भरें");
                return;
            }

            db.insert(date, type, amount, party, note, mode);
        }

        amountInput.setText("");
        partyInput.setText("");
        noteInput.setText("");

        if (type.equals("SALE")) {
            itemRows.removeAllViews();
            itemList.clear();
            addItemRow();
            updateSaleTotal();
        }

        Toast.makeText(
            this,
            "एंट्री सेव हो गई",
            Toast.LENGTH_SHORT
        ).show();

        refresh();
    }

    private String money(double n) {
        return "₹" + String.format(
            new Locale("en", "IN"), "%,.2f", n
        );
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
            TextView empty = text(
                "अभी कोई एंट्री नहीं है। ऊपर से पहली एंट्री जोड़ें।",
                14,
                false
            );
            empty.setPadding(0, dp(8), 0, dp(8));
            entries.addView(empty);
        }

        while (c.moveToNext()) {

            long id = c.getLong(0);
            String date = c.getString(1);
            String type = c.getString(2);
            double amount = c.getDouble(3);
            String party = c.getString(4);
            String note = c.getString(5);
            String mode = c.getString(6);

            LinearLayout card = column();
            card.setBackgroundColor(0xFFFFFFFF);
            card.setPadding(dp(12), dp(8), dp(12), dp(8));

            LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(-1, -2);
            cp.bottomMargin = dp(8);
            entries.addView(card, cp);

            TextView top = text(
                typeLabel(type) + "   •   " + date,
                14,
                true
            );
            card.addView(top);

            TextView mid = text(
                money(amount) + "   •   " +
                (party.isEmpty() ? "बिना नाम" : party),
                16,
                true
            );

            mid.setTextColor(
                type.equals("CASH_PAYMENT")
                ? 0xFFB33A32 : 0xFF17231D
            );
            card.addView(mid);

            // बिक्री के आइटम दिखाएँ
            if (type.equals("SALE")) {
                String itemDetails = db.saleItemsText(id);

                if (!itemDetails.isEmpty()) {
                    TextView itemText = text(itemDetails, 13, false);
                    itemText.setTextColor(0xFF245A9A);
                    itemText.setPadding(0, dp(6), 0, dp(6));
                    card.addView(itemText);
                }
            }

            String detailText =
                ((type.equals("SALE") || type.equals("PURCHASE"))
                ? (mode.equals("CREDIT") ? "उधार" : "Cash") + " • "
                : "") + note;

            TextView detail = text(detailText, 12, false);
            detail.setTextColor(0xFF66756D);
            card.addView(detail);

            Button del = button("यह एंट्री हटाएँ");
            del.setTextSize(12);

            del.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                    .setTitle("एंट्री हटाएँ?")
                    .setMessage("क्या आप यह एंट्री हटाना चाहते हैं?")
                    .setNegativeButton("नहीं", null)
                    .setPositiveButton("हाँ", (d, w) -> {
                        db.delete(id);
                        refresh();
                    })
                    .show()
            );

            card.addView(del);
        }

        c.close();
    }

    private void addStat(String label, String value, int color) {

        LinearLayout r = row();
        r.setBackgroundColor(0xFFFFFFFF);

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.bottomMargin = dp(6);

        summary.addView(r, p);

        TextView l = text(label, 13, false);
        TextView v = text(value, 15, true);
        v.setTextColor(color);

        r.addView(l, new LinearLayout.LayoutParams(0, -2, 1));
        r.addView(v);

        r.setPadding(dp(12), dp(12), dp(12), dp(12));
    }

    private String typeLabel(String t) {
        switch (t) {
            case "SALE": return "बिक्री";
            case "PURCHASE": return "खरीद";
            case "CASH_RECEIPT": return "कैश प्राप्त";
            default: return "कैश भुगतान";
        }
    }

    // बिक्री की एक आइटम लाइन
    private static class SaleItem {
        String name;
        double qty, rate, total;

        SaleItem(String name, double qty, double rate, double total) {
            this.name = name;
            this.qty = qty;
            this.rate = rate;
            this.total = total;
        }
    }

    // स्क्रीन पर एक आइटम की फील्ड
    private static class ItemRow {
        EditText name, qty, rate;
        TextView total;
        LinearLayout card;

        ItemRow(
            EditText name,
            EditText qty,
            EditText rate,
            TextView total,
            LinearLayout card
        ) {
            this.name = name;
            this.qty = qty;
            this.rate = rate;
            this.total = total;
            this.card = card;
        }
    }

    static class DB extends SQLiteOpenHelper {

        DB(Context c) {
            super(c, "bfp_accounting.db", null, 2);
        }

        public void onCreate(SQLiteDatabase d) {

            d.execSQL(
                "CREATE TABLE entries(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "party TEXT," +
                "note TEXT," +
                "mode TEXT NOT NULL)"
            );

            d.execSQL(
                "CREATE TABLE settings(" +
                "k TEXT PRIMARY KEY," +
                "v REAL NOT NULL)"
            );

            d.execSQL(
                "INSERT INTO settings(k,v) VALUES('opening_cash',0)"
            );

            createSaleItemsTable(d);
        }

        private void createSaleItemsTable(SQLiteDatabase d) {
            d.execSQL(
                "CREATE TABLE IF NOT EXISTS sale_items(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "entry_id INTEGER NOT NULL," +
                "item_name TEXT NOT NULL," +
                "qty REAL NOT NULL," +
                "rate REAL NOT NULL," +
                "line_total REAL NOT NULL)"
            );
        }

        public void onUpgrade(
            SQLiteDatabase d,
            int oldV,
            int newV
        ) {
            if (oldV < 2) {
                createSaleItemsTable(d);
            }
        }

        void insert(
            String date,
            String type,
            double amount,
            String party,
            String note,
            String mode
        ) {
            ContentValues v = new ContentValues();
            v.put("date", date);
            v.put("type", type);
            v.put("amount", amount);
            v.put("party", party);
            v.put("note", note);
            v.put("mode", mode);

            getWritableDatabase().insert("entries", null, v);
        }

        // बिक्री और उसके सभी आइटम एक साथ सेव होंगे
        void insertSale(
            String date,
            double amount,
            String party,
            String note,
            String mode,
            ArrayList<SaleItem> items
        ) {
            SQLiteDatabase d = getWritableDatabase();
            d.beginTransaction();

            try {
                ContentValues v = new ContentValues();
                v.put("date", date);
                v.put("type", "SALE");
                v.put("amount", amount);
                v.put("party", party);
                v.put("note", note);
                v.put("mode", mode);

                long entryId = d.insert("entries", null, v);

                if (entryId == -1) {
                    throw new Exception("Sale save failed");
                }

                for (SaleItem item : items) {
                    ContentValues iv = new ContentValues();
                    iv.put("entry_id", entryId);
                    iv.put("item_name", item.name);
                    iv.put("qty", item.qty);
                    iv.put("rate", item.rate);
                    iv.put("line_total", item.total);

                    d.insertOrThrow("sale_items", null, iv);
                }

                d.setTransactionSuccessful();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                d.endTransaction();
            }
        }

        String saleItemsText(long entryId) {

            StringBuilder result = new StringBuilder();

            Cursor c = getReadableDatabase().rawQuery(
                "SELECT item_name,qty,rate,line_total " +
                "FROM sale_items WHERE entry_id=?",
                new String[]{String.valueOf(entryId)}
            );

            while (c.moveToNext()) {
                String name = c.getString(0);
                double qty = c.getDouble(1);
                double rate = c.getDouble(2);
                double total = c.getDouble(3);

                if (result.length() > 0) result.append("\n");

                result.append(name)
                    .append("  | मात्रा: ")
                    .append(qty)
                    .append("  | रेट: ")
                    .append("₹").append(rate)
                    .append("  | कुल: ")
                    .append("₹").append(total);
            }

            c.close();
            return result.toString();
        }

        double sum(String type) {
            Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) " +
                "FROM entries WHERE type=?",
                new String[]{type}
            );

            double n = 0;
            if (c.moveToFirst()) n = c.getDouble(0);
            c.close();
            return n;
        }

        double cashIn() {
            Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) FROM entries " +
                "WHERE (type='SALE' AND mode='CASH') " +
                "OR type='CASH_RECEIPT'",
                null
            );

            double n = 0;
            if (c.moveToFirst()) n = c.getDouble(0);
            c.close();
            return n;
        }

        double cashOut() {
            Cursor c = getReadableDatabase().rawQuery(
                "SELECT COALESCE(SUM(amount),0) FROM entries " +
                "WHERE (type='PURCHASE' AND mode='CASH') " +
                "OR type='CASH_PAYMENT'",
                null
            );

            double n = 0;
            if (c.moveToFirst()) n = c.getDouble(0);
            c.close();
            return n;
        }

        double openingCash() {
            Cursor c = getReadableDatabase().rawQuery(
                "SELECT v FROM settings WHERE k='opening_cash'",
                null
            );

            double n = 0;
            if (c.moveToFirst()) n = c.getDouble(0);
            c.close();
            return n;
        }

        Cursor list() {
            return getReadableDatabase().rawQuery(
                "SELECT id,date,type,amount," +
                "COALESCE(party,''),COALESCE(note,''),mode " +
                "FROM entries ORDER BY id DESC",
                null
            );
        }

        void delete(long id) {
            SQLiteDatabase d = getWritableDatabase();
            d.beginTransaction();

            try {
                d.delete(
                    "sale_items",
                    "entry_id=?",
                    new String[]{String.valueOf(id)}
                );

                d.delete(
                    "entries",
                    "id=?",
                    new String[]{String.valueOf(id)}
                );

                d.setTransactionSuccessful();

            } finally {
                d.endTransaction();
            }
        }
    }
}
