package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.chin.bbdb.DatabaseQuerier;
import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.SearchCriterion;
import com.chin.bbdb.TabListener;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * The activity for advanced search function
 * @author Chin
 *
 */
public class AdvancedSearchActivity extends BaseFragmentActivity {
    // map what's displayed and what's the actual table column behind the scene
    public static HashMap<String, String> uiDbMap = new HashMap<String, String>();
    static
    {
        uiDbMap.put("HP", "popeHp");
        uiDbMap.put("ATK", "popeAtk");
        uiDbMap.put("DEF", "popeDef");
        uiDbMap.put("WIS", "popeWis");
        uiDbMap.put("AGI", "popeAgi");
    }

    // store the result set after each search
    static ArrayList<String> resultSet = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add the tabs
        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        bar.addTab(bar.newTab().setText("Search")
                .setTabListener(new TabListener<SearchCriteriaFragment>(this, "search", SearchCriteriaFragment.class, null)));
        bar.addTab(bar.newTab().setText("Results")
                .setTabListener(new TabListener<SearchResultFragment>(this, "result", SearchResultFragment.class, null)));

        // if we're resuming the activity, re-select the tab that was selected before
        if (savedInstanceState != null) {
            // Select the tab that was selected before orientation change
            int index = savedInstanceState.getInt("TAB_INDEX");
            bar.setSelectedNavigationItem(index);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
      super.onSaveInstanceState(bundle);
      // Save the index of the currently selected tab
      bundle.putInt("TAB_INDEX", getActionBar().getSelectedTab().getPosition());
    }

    /**
     * Fragment for the search criteria
     */
    public static class SearchCriteriaFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_advanced_search_criteria, container, false);
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.fragment_layout);

            // the id lists for the subject spinners, operator spinners and criteria editTexts
            // they should all contain the same number of items
            int[] spinnerSubjectIdList = new int[] {
                R.id.spinnerSubject1, R.id.spinnerSubject2, R.id.spinnerSubject3,
                R.id.spinnerSubject4, R.id.spinnerSubject5
            };
            int[] spinnerOperatorIdList = new int[] {
                R.id.spinnerOperator1, R.id.spinnerOperator2, R.id.spinnerOperator3,
                R.id.spinnerOperator4, R.id.spinnerOperator5
            };
            int[] editTextIdList = new int[] {
                R.id.criteria1, R.id.criteria2, R.id.criteria3,
                R.id.criteria4, R.id.criteria5
            };

            // list of the actual controls
            final ArrayList<Spinner> spinnerSubjectList = new ArrayList<Spinner>();
            final ArrayList<Spinner> spinnerOperatorList = new ArrayList<Spinner>();
            final ArrayList<EditText> editTextList = new ArrayList<EditText>();

            ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[] {
                    "", "HP", "ATK", "DEF", "WIS", "AGI"
            });
            // Specify the layout to use when the list of choices appears
            subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<String> operatorAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[] {
                    ">", ">=", "<", "<=", "="
            });
            // Specify the layout to use when the list of choices appears
            operatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for (int i = 0; i < spinnerSubjectIdList.length; i++) {
                Spinner subjectSpin = (Spinner) layout.findViewById(spinnerSubjectIdList[i]);
                subjectSpin.setAdapter(subjectAdapter);
                spinnerSubjectList.add(subjectSpin);

                final Spinner operatorSpin = (Spinner) layout.findViewById(spinnerOperatorIdList[i]);
                operatorSpin.setAdapter(operatorAdapter);
                spinnerOperatorList.add(operatorSpin);

                final EditText criteriaText = (EditText) layout.findViewById(editTextIdList[i]);
                editTextList.add(criteriaText);
                if (subjectSpin.getSelectedItem().toString() == "") {
                    operatorSpin.setEnabled(false);
                    criteriaText.setEnabled(false);
                }

                // disable or enable the operator and criteria based on whether the subject s blank or not
                subjectSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selected = (String) parent.getItemAtPosition(position);
                        if (selected == "") {
                            operatorSpin.setEnabled(false);
                            criteriaText.setEnabled(false);
                        }
                        else {
                            operatorSpin.setEnabled(true);
                            criteriaText.setEnabled(true);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            Button searchButton = (Button) layout.findViewById(R.id.searchButton);
            searchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // make the list of criteria
                    ArrayList<SearchCriterion> criteriaList = new ArrayList<SearchCriterion>();
                    for (int i = 0; i < spinnerSubjectList.size(); i++) {
                        String subject = uiDbMap.get(spinnerSubjectList.get(i).getSelectedItem().toString());
                        String operator = spinnerOperatorList.get(i).getSelectedItem().toString();
                        String object = editTextList.get(i).getText().toString();

                        if (subject == null || subject.equals("") || operator.equals("") || object.equals("")) {
                            continue;
                        }
                        criteriaList.add(new SearchCriterion(subject, operator, object));
                    }

                    // then execute the query and get the result
                    DatabaseQuerier querier = new DatabaseQuerier(v.getContext());
                    resultSet = querier.executeQuery(SearchCriterion.getCriteria(criteriaList));
                    Collections.sort(resultSet);

                    // now display the results
                    if (!resultSet.isEmpty()) {
                        // switch to the result tab (with index 1)
                        getActivity().getActionBar().setSelectedNavigationItem(1);
                        Toast toast = Toast.makeText(getActivity(), "Found " + resultSet.size() + " results.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else {
                        Toast toast = Toast.makeText(getActivity(), "No results found.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            });

            return view;
        }
    }

    /**
     * Fragment for the search result
     */
    public static class SearchResultFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_search_result, container, false);
            ListView famListView = (ListView) view.findViewById(R.id.resultListView);

            // set the result set of the previous search as the adapter for the list
            famListView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, resultSet));

            // go to a fam's detail page when click on its name on the list
            famListView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                        String famName = (String)arg0.getItemAtPosition(position);
                        Intent intent = new Intent(v.getContext(), FamDetailActivity.class);
                        intent.putExtra(MainActivity.FAM_NAME, famName);
                        intent.putExtra(MainActivity.FAM_LINK, FamStore.famLinkTable.get(famName));
                        startActivity(intent);
                }
            });
            return view;
        }
    }
}
