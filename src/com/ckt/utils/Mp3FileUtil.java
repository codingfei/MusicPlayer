package com.ckt.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.ckt.modle.Mp3Info;

/**
 * mp3文件的工具类,包括获取歌手图片,歌手信息,获取文件大小等方法
 *
 * @author JonsonMarxy
 */
public class Mp3FileUtil {
    //读取音乐图片时的uri
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    /**
     * 获取文件大小
     *
     * @param f 指定的文件路径
     * @return
     */
    public static double getFileSize(File f) {
        // 计算文件大小
        double fl = (double) f.length() / 1024 / 1024;
        // 格式化fl,只保留两位小数
        fl = new BigDecimal(fl).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        return fl;
    }

    //将long型的时长,格式化为xx:xx格式的字符串
    public static String getDuringString(long during) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            Date date = new Date(during);
            return dateFormat.format(date);
        } catch (Exception e) {
            // TODO: handle exception
            return "";
        }
    }


    //从contentProvider里面读取歌曲列表---建议开个线程读取
    public static ArrayList<Mp3Info> getMp3InfoList(Context context) {
        ArrayList<Mp3Info> list = new ArrayList<Mp3Info>();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        try {
            while (true) {
                int isMusic = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)); // 是否为音乐
                if (isMusic != 0) { //是音乐文件
                    String title = cursor.getString((cursor
                            .getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
                    String artist = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
                    String album = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ALBUM)); // 专辑
                    long duration = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
                    String path = cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
                    long song_id = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media._ID));  //音乐ID
                    long album_id = cursor.getLong(cursor
                            .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));  //专辑ID
                    double fileSize = getFileSize(new File(path));

                    //文件过滤
                    if (duration > 40 * 1000 || duration == 0) {
                        Mp3Info mp3Info = new Mp3Info(song_id, album_id, album, title, artist, fileSize,
                                duration, path);
                        list.add(mp3Info);
                    }
                }
                if (cursor.isLast()) break;
                cursor.moveToNext();
            }
        } catch (Exception e) {

        }
        return list;
    }


    //获取指定音乐专辑-->音乐ID的音乐图片
    public static Bitmap getMusicBitpMap(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            return null;
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {

                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {
        }
        return bm;
    }
}
