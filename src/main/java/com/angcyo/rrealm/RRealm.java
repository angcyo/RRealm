package com.angcyo.rrealm;

import android.app.Application;
import android.os.Looper;

import com.angcyo.library.utils.L;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmModel;
import io.realm.RealmObject;
import rx.functions.Action1;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/24 9:13
 * 修改人员：Robi
 * 修改时间：2016/12/24 9:13
 * 修改备注：
 * Version: 1.0.0
 */
public class RRealm {

    private Realm mRealm;

    private RRealm() {
    }

    public static RRealm instance() {
        return Holder.instance;
    }

    /**
     * 同步的方式保存一个realm对象
     */
    public static <R extends RealmObject> void save(R object) {
        if (object == null) {
            return;
        }
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                realm.copyToRealm(object);
            } else {
                realm.beginTransaction();
                realm.copyToRealm(object);
                realm.commitTransaction();
            }
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }

        L.i("保存至数据库:" + object.toString());
    }

    /**
     * 同步的方式保存一组realm对象
     */
    public static <R extends RealmObject> void save(Iterable<R> objects) {
        if (objects == null) {
            return;
        }
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                realm.copyToRealm(objects);
            } else {
                realm.beginTransaction();
                realm.copyToRealm(objects);
                realm.commitTransaction();
            }
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    /**
     * 删除指定表的所有数据
     */
    public static <E extends RealmModel> void delete(final Class<E> clazz) {
//        RRealm.where(new Action1<Realm>() {
//            @Override
//            public void call(Realm realm) {
//                realm.where(clazz).findAll().deleteAllFromRealm();
//            }
//        });

        RRealm.exe(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(clazz);
            }
        });
    }

    /**
     * 同步执行,并出现错误自动回滚事务
     */
    public static void exe(final Realm.Transaction transaction) {
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                transaction.execute(realm);
            } else {
                realm.executeTransaction(transaction);
            }
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    /**
     * 异步执行,并出现错误自动回滚事务
     */
    public static void async(final Realm.Transaction transaction) {
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                transaction.execute(realm);
            } else {
                realm.executeTransactionAsync(transaction);
            }
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    /**
     * 异步执行,并出现错误自动回滚事务
     */
    public static void async(final Realm.Transaction transaction, final Realm.Transaction.OnSuccess onSuccess) {
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                transaction.execute(realm);
                onSuccess.onSuccess();
            } else {
                realm.executeTransactionAsync(transaction, onSuccess);
            }
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    public static Realm realm() {
        return instance().getRealm();
    }

    /**
     * 异步执行,并出现错误自动回滚事务
     */
    public static void async(final Realm.Transaction transaction, final Realm.Transaction.OnSuccess onSuccess, final Realm.Transaction.OnError onError) {
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            realm.executeTransactionAsync(transaction, onSuccess, onError);
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    public static void where(Action1<Realm> action) {
        Realm realm = isMainThread() ? realm() : getRealmInstance();
        try {
            if (realm.isInTransaction()) {
                action.call(realm);
            } else {
                realm.beginTransaction();
                action.call(realm);
                realm.commitTransaction();
            }
        } catch (Exception e) {
            realm.cancelTransaction();
        } finally {
            if (!isMainThread()) {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }

    public static void init(final Application application) {
        init(application, application.getPackageName());
    }

    /**
     * 初始化
     */
    public static void init(final Application application, String name) {
        Realm.init(application);
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder()
                .name(name)
                .migration(new RealmMigration() {
                    @Override
                    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
                        L.e("数据库升级 Start:" + oldVersion + "->" + newVersion);
//                        RealmSchema schema = realm.getSchema();
//                        realm.removeAllChangeListeners();
//                        realm.deleteAll();
//                        schema.close();
//                        Realm.removeDefaultConfiguration();
//                        init(application);
                        L.e("数据库升级 End:" + oldVersion + "->" + newVersion);

//                        if (oldVersion == 0) {
//                            schema.create("Person")
//                                    .addField("name", String.class)
//                                    .addField("age", int.class);
//                            oldVersion++;
//                        }
//
//                        if (oldVersion == 1) {
//                            schema.get("Person")
//                                    .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
//                                    .addRealmObjectField("favoriteDog", schema.get("Dog"))
//                                    .addRealmListField("dogs", schema.get("Dog"));
//                            oldVersion++;
//                        }
                    }
                })
                .schemaVersion(1);

        RealmConfiguration config;
//        if (BuildConfig.SHOW_DEBUG) {
        config = builder.deleteRealmIfMigrationNeeded().build();
//        } else {
//            config = builder.build();
//        }

        Realm.setDefaultConfiguration(config);
    }

    /**
     * 主线程的Realm实例不关闭
     */
    public static boolean isMainThread() {
//        return Looper.getMainLooper().getThread() == Thread.currentThread();
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 返回一个新的实例, 请在子线程访问Realm的时候调用,主线程使用 {@link #getRealm()}
     */
    public static Realm getRealmInstance() {
        return Realm.getDefaultInstance();
    }

    public Realm getRealm() {
        if (!isMainThread()) {
            throw new IllegalArgumentException("请在主线程调用, 子线程请直接调用 getRealmInstance(),并自行close");
        }
        if (mRealm == null || mRealm.isClosed()) {
            mRealm = Realm.getDefaultInstance();
        }
        return mRealm;
    }

    private static class Holder {
        static RRealm instance = new RRealm();
    }
}
