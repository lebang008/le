package com.leday.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.leday.R;
import com.leday.Util.PreferenUtil;
import com.leday.adapter.NoteAdapter;
import com.leday.entity.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/10/26.
 */
public class NoteActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private TextView mTitle;

    private List<Note> mList = new ArrayList<>();
    private NoteAdapter mAdapter;

    private SQLiteDatabase mDatabase;

    @Override
    protected void onRestart() {
        super.onRestart();
        mList.clear();
        initData();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        initView();
    }

    private void initView() {
        mTitle = (TextView) findViewById(R.id.txt_note_title);
        mListView = (ListView) findViewById(R.id.listview_activity_note);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);

        initData();

        mAdapter = new NoteAdapter(this, mList);
        mListView.setAdapter(mAdapter);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (!PreferenUtil.contains(NoteActivity.this, "notetb_is_exist")) {
            mTitle.setText("还没有便签，新建一个吧");
            return;
        }
        mDatabase = openOrCreateDatabase("leday.db", MODE_PRIVATE, null);
        //数据库查询
        Cursor mCursor = mDatabase.query("notetb", null, "_id>?", new String[]{"0"}, null, null, "date desc");
        if (mCursor != null) {
            Note note;
            while (mCursor.moveToNext()) {
                note = new Note();
                note.setTime(mCursor.getString(mCursor.getColumnIndex("date")));
                note.setTitle(mCursor.getString(mCursor.getColumnIndex("title")));
                note.setContent(mCursor.getString(mCursor.getColumnIndex("content")));
                mList.add(note);
            }
            mCursor.close();
        }
        mDatabase.close();
    }

    /**
     * 修改便签风格
     *
     * @param view
     */
    public void doChange(View view) {
        Snackbar.make(view, "修改便签底色风格", Snackbar.LENGTH_SHORT)
                .setActionTextColor(Color.parseColor("#3f51b5"))
                .setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(NoteActivity.this)
                                .setTitle("选择便签风格")
                                .setPositiveButton("默认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenUtil.put(NoteActivity.this, "note_style", "violet");
                                        startActivity(new Intent(NoteActivity.this, NoteDetailActivity.class));
                                    }
                                })
                                .setNegativeButton("经典", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenUtil.put(NoteActivity.this, "note_style", "white");
                                        startActivity(new Intent(NoteActivity.this, NoteDetailActivity.class));
                                    }
                                }).show();
                    }
                }).show();
    }

    /**
     * 新建便签
     *
     * @param view
     */
    public void doWrite(View view) {
        startActivity(new Intent(NoteActivity.this, NoteDetailActivity.class));
    }

    public void doBack(View view) {
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra("local_date", mList.get(position).getTime());
        intent.putExtra("local_title", mList.get(position).getTitle());
        intent.putExtra("local_content", mList.get(position).getContent());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle("该操作将删除这条便签")
                .setMessage("确认删除吗?")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabase = openOrCreateDatabase("leday.db", MODE_PRIVATE, null);
                        String local_delete = "DELETE FROM notetb WHERE date = '" + mList.get(position).getTime() + "'";
                        mDatabase.execSQL(local_delete);
                        mDatabase.close();

                        //TODO 解决:删除操作后，刷新列表的position出現错乱Bug
                        //旧数据清楚
                        mList.clear();
                        //新数据填充
                        initData();
                        //适配器 set change
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        return true;
    }
}