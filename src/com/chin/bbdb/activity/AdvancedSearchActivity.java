package com.chin.bbdb.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import com.chin.bbdb.DatabaseQuerier;
import com.chin.bbdb.FamStore;
import com.chin.bbdb.R;
import com.chin.bbdb.SearchCriterion;
import com.chin.common.TabListener;

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
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity for advanced search function
 * @author Chin
 *
 */
public class AdvancedSearchActivity extends BaseFragmentActivity {
    // map what's displayed and what's the actual table column behind the scene
    public static HashMap<String, String> uiDbMap = new HashMap<String, String>();
    static {
        uiDbMap.put("HP", "popeHp");
        uiDbMap.put("ATK", "popeAtk");
        uiDbMap.put("DEF", "popeDef");
        uiDbMap.put("WIS", "popeWis");
        uiDbMap.put("AGI", "popeAgi");
        uiDbMap.put("Skill type", "skillType");
        uiDbMap.put("Skill function", "skillFunc");
        uiDbMap.put("Skill range", "skillRange");
    }

    public static HashMap<String, String> skillTypeMap = new HashMap<String, String>();
    static {
        skillTypeMap.put("Opening", "1");
        skillTypeMap.put("Active", "2");
        skillTypeMap.put("Reactive", "356");
        skillTypeMap.put("On death", "16");
    }

    public static HashMap<String, String> skillFuncMap = new HashMap<String, String>();
    static {
        skillFuncMap.put("Buff", "1");
        skillFuncMap.put("Debuff", "2");
        skillFuncMap.put("Attack", "3");
        skillFuncMap.put("Attack PI", "4");
        skillFuncMap.put("Attack in tandom", "5");
        skillFuncMap.put("Revive", "6");
        skillFuncMap.put("Kill", "7");
        skillFuncMap.put("Focus", "9");
        skillFuncMap.put("Drain", "11");
        skillFuncMap.put("Protect", "12");
        skillFuncMap.put("Counter", "13");
        skillFuncMap.put("Protect & Counter", "14");
        skillFuncMap.put("Dispell", "16");
        skillFuncMap.put("Suicide", "17");
        skillFuncMap.put("Heal", "18");
        skillFuncMap.put("Affliction", "19");
        skillFuncMap.put("Survive", "20");
        skillFuncMap.put("Debuff attack", "21");
        skillFuncMap.put("Debuff attack PI", "22");
        skillFuncMap.put("Random", "24");
        skillFuncMap.put("Mimic", "25");
        skillFuncMap.put("Imitate", "26");
        skillFuncMap.put("Evade", "27");
        skillFuncMap.put("Reflect", "28");
        skillFuncMap.put("Onhit dispell", "29");
        skillFuncMap.put("Turn order change", "31");
        skillFuncMap.put("CB debuff", "32");
        skillFuncMap.put("CB debuff attack", "33");
        skillFuncMap.put("CB debuff attack PI", "34");
        skillFuncMap.put("Drain attack", "36");
        skillFuncMap.put("Drain attack PI", "37");
        skillFuncMap.put("Onhit debuff", "38");
        skillFuncMap.put("Clear debuff", "40");
    }

    public static HashMap<String, String> skillRangeMap = new HashMap<String, String>();
    static {
        skillRangeMap.put("Either side", "1");
        skillRangeMap.put("Both sides", "2");
        skillRangeMap.put("Self & both sides", "3");
        skillRangeMap.put("All friend", "4");
        skillRangeMap.put("1 near enemy", "5");
        skillRangeMap.put("2 near enemies", "6");
        skillRangeMap.put("3 near enemies", "7");
        skillRangeMap.put("All enemies", "8");
        skillRangeMap.put("1 rear enemy", "11");
        skillRangeMap.put("Front enemies", "12");
        skillRangeMap.put("Mid enemies", "13");
        skillRangeMap.put("Rear enemies", "14");
        skillRangeMap.put("Front & mid enemies", "15");
        skillRangeMap.put("3 random enemies", "16");
        skillRangeMap.put("6 random enemies", "17");
        skillRangeMap.put("3 random rear enemies", "18");
        skillRangeMap.put("4 random enemies", "19");
        skillRangeMap.put("5 random enemies", "20");
        skillRangeMap.put("Myself", "21");
        skillRangeMap.put("2 random enemies", "23");
        skillRangeMap.put("Right", "28");
        skillRangeMap.put("4 near enemies", "32");
        skillRangeMap.put("Front & rear enemies", "34");
        skillRangeMap.put("1 random friend", "101");
        skillRangeMap.put("3 random friends or self", "113");
        skillRangeMap.put("1 random friend.", "121");
        skillRangeMap.put("2 random unique friends", "122");
        skillRangeMap.put("2 random unique friends or self", "132");
        skillRangeMap.put("All enemies scaled", "208");
        skillRangeMap.put("3 near enemies scaled", "313");
        skillRangeMap.put("4 near enemies scaled", "314");
        skillRangeMap.put("4 random enemies varying", "419");
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
                .setTabListener(new TabListener<SearchCriteriaFragment>(this, "search", SearchCriteriaFragment.class, null, R.id.tab_viewgroup)));
        bar.addTab(bar.newTab().setText("Results")
                .setTabListener(new TabListener<SearchResultFragment>(this, "result", SearchResultFragment.class, null, R.id.tab_viewgroup)));

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

            initializeSkillSearchUI(layout);

            Button searchButton = (Button) layout.findViewById(R.id.searchButton);
            searchButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // make the list of criteria
                    ArrayList<SearchCriterion> famCriteriaList = new ArrayList<SearchCriterion>();
                    for (int i = 0; i < spinnerSubjectList.size(); i++) {
                        String subject = uiDbMap.get(spinnerSubjectList.get(i).getSelectedItem().toString());
                        String operator = spinnerOperatorList.get(i).getSelectedItem().toString();
                        String object = editTextList.get(i).getText().toString();

                        if (subject == null || subject.equals("") || operator.equals("") || object.equals("")) {
                            continue;
                        }
                        famCriteriaList.add(new SearchCriterion(subject, operator, object));
                    }

                    ArrayList<SearchCriterion> skillCriteriaList = new ArrayList<SearchCriterion>();

                    Spinner skillTypeChoiceSpin = (Spinner) v.getRootView().findViewById(R.id.spinnerSkillTypeChoice);
                    if (skillTypeChoiceSpin.isEnabled()) {
                        String skillType = skillTypeMap.get(skillTypeChoiceSpin.getSelectedItem().toString());
                        skillCriteriaList.add(new SearchCriterion("skillType", "=", skillType));
                    }

                    Spinner skillFuncChoiceSpin = (Spinner) v.getRootView().findViewById(R.id.spinnerSkillFuncChoice);
                    if (skillFuncChoiceSpin.isEnabled()) {
                        String skillFunc = skillFuncMap.get(skillFuncChoiceSpin.getSelectedItem().toString());
                        skillCriteriaList.add(new SearchCriterion("skillFunc", "=", skillFunc));
                    }

                    Spinner skillRangeChoiceSpin = (Spinner) v.getRootView().findViewById(R.id.spinnerSkillRangeChoice);
                    if (skillRangeChoiceSpin.isEnabled()) {
                        String skillRange = skillRangeMap.get(skillRangeChoiceSpin.getSelectedItem().toString());
                        skillCriteriaList.add(new SearchCriterion("skillRange", "=", skillRange));
                    }

                    // then execute the query and get the result
                    DatabaseQuerier querier = new DatabaseQuerier(v.getContext());
                    resultSet = querier.executeQuery(SearchCriterion.getCriteria(famCriteriaList), SearchCriterion.getCriteria(skillCriteriaList));
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

        private void initializeSkillSearchUI(View layout) {
            // skillType
            ArrayAdapter<String> skillTypeSpinAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[] {
                    "", "Skill type"
            });
            skillTypeSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner skillTypeSpin = (Spinner) layout.findViewById(R.id.spinnerSkillType);
            skillTypeSpin.setAdapter(skillTypeSpinAdapter);

            final TextView tvSkillTypeOp = (TextView) layout.findViewById(R.id.textViewSkillTypeOperator);
            final Spinner skillTypeChoiceSpin = (Spinner) layout.findViewById(R.id.spinnerSkillTypeChoice);
            ArrayAdapter<String> skillTypeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, new String[] {
                    "Opening", "Active", "Reactive", "On death"
            });
            skillTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            skillTypeChoiceSpin.setAdapter(skillTypeAdapter);
            if (skillTypeSpin.getSelectedItem().toString() == "") {
                tvSkillTypeOp.setEnabled(false);
                skillTypeChoiceSpin.setEnabled(false);
            }

            skillTypeSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = (String) parent.getItemAtPosition(position);
                    if (selected == "") {
                        tvSkillTypeOp.setEnabled(false);
                        skillTypeChoiceSpin.setEnabled(false);
                    }
                    else {
                        tvSkillTypeOp.setEnabled(true);
                        skillTypeChoiceSpin.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // skillFunc
            ArrayAdapter<String> skillFuncSpinAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[] {
                    "", "Skill function"
            });
            skillFuncSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner skillFuncSpin = (Spinner) layout.findViewById(R.id.spinnerSkillFunc);
            skillFuncSpin.setAdapter(skillFuncSpinAdapter);

            final TextView tvSkillFuncOp = (TextView) layout.findViewById(R.id.textViewSkillFuncOperator);
            final Spinner skillFuncChoiceSpin = (Spinner) layout.findViewById(R.id.spinnerSkillFuncChoice);

            // turn the keys of the skillFuncMap into an array
            ArrayList<String> funcs = new ArrayList<String>(Arrays.asList(skillFuncMap.keySet().toArray(new String[skillFuncMap.keySet().size()])));
            Collections.sort(funcs);
            ArrayAdapter<String> skillFuncAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, funcs);
            skillFuncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            skillFuncChoiceSpin.setAdapter(skillFuncAdapter);

            if (skillFuncSpin.getSelectedItem().toString() == "") {
                tvSkillFuncOp.setEnabled(false);
                skillFuncChoiceSpin.setEnabled(false);
            }

            skillFuncSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = (String) parent.getItemAtPosition(position);
                    if (selected == "") {
                        tvSkillFuncOp.setEnabled(false);
                        skillFuncChoiceSpin.setEnabled(false);
                    }
                    else {
                        tvSkillFuncOp.setEnabled(true);
                        skillFuncChoiceSpin.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // skillRange
            ArrayAdapter<String> skillRangeSpinAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item, new String[] {
                    "", "Skill range"
            });
            skillRangeSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner skillRangeSpin = (Spinner) layout.findViewById(R.id.spinnerSkillRange);
            skillRangeSpin.setAdapter(skillRangeSpinAdapter);

            final TextView tvSkillRangeOp = (TextView) layout.findViewById(R.id.textViewSkillRangeOperator);
            final Spinner skillRangeChoiceSpin = (Spinner) layout.findViewById(R.id.spinnerSkillRangeChoice);

            // turn the keys of the skillRangeMap into an array
            ArrayList<String> ranges = new ArrayList<String>(Arrays.asList(skillRangeMap.keySet().toArray(new String[skillRangeMap.keySet().size()])));
            Collections.sort(ranges);
            ArrayAdapter<String> skillRangeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, ranges);
            skillRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            skillRangeChoiceSpin.setAdapter(skillRangeAdapter);

            if (skillRangeSpin.getSelectedItem().toString() == "") {
                tvSkillRangeOp.setEnabled(false);
                skillRangeChoiceSpin.setEnabled(false);
            }

            skillRangeSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = (String) parent.getItemAtPosition(position);
                    if (selected == "") {
                        tvSkillRangeOp.setEnabled(false);
                        skillRangeChoiceSpin.setEnabled(false);
                    }
                    else {
                        tvSkillRangeOp.setEnabled(true);
                        skillRangeChoiceSpin.setEnabled(true);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
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
