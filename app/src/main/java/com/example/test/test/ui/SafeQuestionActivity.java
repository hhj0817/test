package com.example.test.test.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.test.R;
import com.example.test.test.manager.AppLockManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.example.test.test.ui.UnlockActivity.TYPE_RESET;

public class SafeQuestionActivity extends AppCompatActivity {

    public static final String FROM_WHERE = "from_where";
    //在哪跳转的SafeQuestionActivity
    public static final int FROM_FIRST_ENTER = 100;
    public static final int FROM_ENTER = 200;
    public static final int FROM_UNLOCK = 300;
    public static final int FROM_SETTING = 400;


    @BindView(R.id.tool_bar)
    Toolbar mToolBar;
    @BindView(R.id.tv_question)
    TextView mTvQuestion;
    @BindView(R.id.et_answer)
    EditText mEtAnswer;
    @BindView(R.id.btn_confirm)
    TextView mBtnConfirm;

    private int from;
    private AppLockManager mAppLockManager;
    private int mCurrentQuestionIndex;
    private String[] mQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_question);
        ButterKnife.bind(this);
        initToolBar();
        init();
        initView();
    }

    private void init() {
        Intent intent = getIntent();
        from = intent.getIntExtra(FROM_WHERE, FROM_UNLOCK);
        mAppLockManager = AppLockManager.getInstance();
        mCurrentQuestionIndex = mAppLockManager.getSafeQuestionIndex();
        mQuestions = getResources().getStringArray(R.array.safe_questions);
    }

    private void initView() {
        mTvQuestion.setText(mQuestions[mCurrentQuestionIndex]);
    }

    private void initToolBar() {
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick({R.id.tv_question, R.id.btn_spinner, R.id.btn_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //弹出安全问题选择框
            case R.id.tv_question:
            case R.id.btn_spinner:
                showQuestionSelectDialog(mAppLockManager.getSafeQuestionIndex());
                break;
            //确认按钮
            case R.id.btn_confirm:
                String answer = mEtAnswer.getText().toString().trim();
                switch (from) {
                    case FROM_FIRST_ENTER:
                        if (TextUtils.isEmpty(answer)) {
                            Toast.makeText(this, R.string.safe_ans_empty, Toast.LENGTH_SHORT).show();
                        } else {
                            mAppLockManager.setSafeQuestionAnswer(this,answer);
                            mAppLockManager.setSafeQuestionIndex(this,mCurrentQuestionIndex);
                            gotoAppLockActivity();
                            finish();
                        }
                        break;
                    case FROM_ENTER:
                    case FROM_UNLOCK:
                        String safeQuestionAnswer = mAppLockManager.getSafeQuestionAnswer();
                        int safeQuestionIndex = mAppLockManager.getSafeQuestionIndex();
                        if (answer.equals(safeQuestionAnswer) && mCurrentQuestionIndex == safeQuestionIndex) {
                            resetPassword();
                            finish();
                        } else {
                            Toast.makeText(this, R.string.wrong_ans, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case FROM_SETTING:
                        if (TextUtils.isEmpty(answer)) {
                            Toast.makeText(this, R.string.safe_ans_empty, Toast.LENGTH_SHORT).show();
                        } else {
                            finish();
                        }
                        break;
                }
        }
    }

    private void resetPassword() {
        Intent intent = new Intent(this, UnlockActivity.class);
        intent.putExtra(UnlockActivity.TYPE, TYPE_RESET);
        startActivity(intent);
    }

    private void showQuestionSelectDialog(int checkItem) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(R.array.safe_questions, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentQuestionIndex = which;
                        mTvQuestion.setText(mQuestions[which]);
                        dialog.dismiss();
                    }
                })
                .create();
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        alertDialog.show();
    }

    private void gotoAppLockActivity() {
        Intent intent = new Intent(this, AppLockActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
