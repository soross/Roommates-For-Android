package com.gjermundbjaanes.apps.roommates2.expenses;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gjermundbjaanes.apps.roommates2.R;
import com.gjermundbjaanes.apps.roommates2.helpers.Utils;
import com.gjermundbjaanes.apps.roommates2.helpers.adapteritems.EntryItemForExpenses;
import com.gjermundbjaanes.apps.roommates2.helpers.adapteritems.Item;
import com.gjermundbjaanes.apps.roommates2.helpers.adapteritems.SectionItem;
import com.gjermundbjaanes.apps.roommates2.parsesubclasses.Expense;
import com.gjermundbjaanes.apps.roommates2.parsesubclasses.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

class ExpenseAdapter extends BaseAdapter {
    private final Context context;

    private final ArrayList<Item> items = new ArrayList<Item>();
    private ArrayList<Expense> expenses = new ArrayList<Expense>();


    ExpenseAdapter(Context context) {
        this.context = context;
        loadObjects();
    }

    void reloadElements() {
        if (expenses != null) {
            items.clear();

            ArrayList<Expense> paidUpElements = getPaidUpElements();
            ArrayList<Expense> unpaidElements = getUnpaidElements(paidUpElements);

            setUpUnpaidSection(unpaidElements);
            setUpPaidUpSection(paidUpElements);


            notifyDataSetChanged();
        }
    }

    private ArrayList<Expense> getPaidUpElements() {
        ArrayList<Expense> paidUpElements = new ArrayList<Expense>();

        for (Expense expense : expenses) {
            ArrayList<User> notPaidUpUsers = expense.getNotPaidUp();
            if (notPaidUpUsers.isEmpty()) {
                paidUpElements.add(expense);
            }
        }

        return paidUpElements;
    }

    private ArrayList<Expense> getUnpaidElements(ArrayList<Expense> paidUpElements) {
        ArrayList<Expense> unPaidElements = new ArrayList<Expense>(expenses);
        unPaidElements.removeAll(paidUpElements);
        return unPaidElements;
    }

    private void setUpUnpaidSection(ArrayList<Expense> unpaidElements) {
        items.add(new SectionItem(context.getString(R.string.unpaid_section_title)));
        for (Expense expense : unpaidElements) {
            items.add(new EntryItemForExpenses(expense.getName(),
                    context.getString(R.string.created_by) + expense.getOwed().getDisplayName(), expense));
        }
    }

    private void setUpPaidUpSection(ArrayList<Expense> paidUpElements) {
        items.add(new SectionItem(context.getString(R.string.paid_section_title)));
        for (Expense expense : paidUpElements) {
            items.add(new EntryItemForExpenses(expense.getName(),
                    context.getString(R.string.created_by) + expense.getOwed().getDisplayName(), expense));
        }
    }

    public void loadObjects() {
        ParseQuery<Expense> expenseParseQuery = ParseQuery.getQuery(Expense.class);
        expenseParseQuery.include("createdBy");
        expenseParseQuery.include("owed");
        expenseParseQuery.orderByAscending("createdAt");
        expenseParseQuery.whereEqualTo("household", User.getCurrentUser().getActiveHousehold());

        Utils.setSafeQueryCaching(expenseParseQuery);

        expenseParseQuery.findInBackground(new FindCallback<Expense>() {
            @Override
            public void done(List<Expense> expenseList, ParseException e) {
                if (e == null) {
                    expenses = new ArrayList<Expense>(expenseList);
                    reloadElements();
                }
            }
        });
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        Item item = items.get(i);

        if (!item.isSection()) {
            EntryItemForExpenses entryItem = (EntryItemForExpenses) item;
            return entryItem.getExpense();
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Item item = items.get(position);

        if (item != null && item.isSection()) {
            view = setUpSectionItemView((SectionItem) item);
        } else {
            view = setUpEntryItemView((EntryItemForExpenses) item);
        }

        return view;
    }

    private View setUpEntryItemView(EntryItemForExpenses entryItemForExpense) {
        View view = View.inflate(context, R.layout.list_expenses_layout, null);

        TextView title = (TextView) view.findViewById(R.id.textViewList);
        title.setText(entryItemForExpense.getTitle());

        TextView subTitle = (TextView) view.findViewById(R.id.textViewSubTitle);
        subTitle.setText(getSubTitleText(entryItemForExpense));

        return view;
    }

    private String getSubTitleText(EntryItemForExpenses entryItemForExpense) {
        Expense expense = entryItemForExpense.getExpense();

        DecimalFormat df = new DecimalFormat(".00");
        double amountOwedByEachPerson = getAmountOwedByEachPerson(expense);

        if (currentUserIsTheOwedForExpense(expense) && expenseIsNotSettled(expense)) {
            double amountOwedToYou = amountOwedByEachPerson * expense.getNumberOfPeopleNotPaidUp();

            return context.getString(R.string.subtitle_you_are_owed_1) + " " +
                    df.format(amountOwedToYou) + " " +
                    context.getString(R.string.subtitle_you_are_owed_2);
        } else if (expenseIsNotSettled(expense) && currentUserOwesForExpense(expense)) {
            return context.getString(R.string.subtitle_you_owe) + " " +
                    df.format(amountOwedByEachPerson) + " " +
                    context.getString(R.string.subtitle_you_owe_2);
        } else if (expenseIsNotSettled(expense)) {
            return context.getString(R.string.subtitle_you_do_not_owe_anything);
        } else {
            return context.getString(R.string.subtitle_expense_is_settled);
        }
    }

    private double getAmountOwedByEachPerson(Expense expense) {
        double totalAmount = expense.getTotalAmount().doubleValue();
        double numberOfPeople = expense.getNumberOfPeopleInExpense();

        return (totalAmount / numberOfPeople);
    }

    private boolean currentUserIsTheOwedForExpense(Expense expense) {
        String owedPersonObjectId = expense.getOwed().getObjectId();
        String currentUserObjectId = User.getCurrentUser().getObjectId();

        return currentUserObjectId.equals(owedPersonObjectId);
    }

    private boolean expenseIsNotSettled(Expense expense) {
        return expense.getNotPaidUp().size() > 0;
    }

    private boolean currentUserOwesForExpense(Expense expense) {
        User currentUser = User.getCurrentUser();
        for (User notPaidUpUser : expense.getNotPaidUp()) {
            if (notPaidUpUser.getObjectId().equals(currentUser.getObjectId())) {
                return true;
            }
        }

        return false;
    }

    private View setUpSectionItemView(SectionItem item) {
        View view = View.inflate(context, R.layout.list_element_section, null);
        view.setBackgroundColor(Color.LTGRAY);
        view.setOnClickListener(null);
        view.setOnLongClickListener(null);
        view.setLongClickable(false);
        TextView title = (TextView) view.findViewById(R.id.sectionTitleTextView);

        title.setText(item.getTitle());
        return view;
    }

}
