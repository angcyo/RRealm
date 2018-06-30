##### 1: 使用Realm时, 通常需要设置一个config对象,如下:

```
RealmConfiguration config = new RealmConfiguration.Builder()
  .name("myrealm.realm")   //默认路径下的数据库文件名
  .encryptionKey(getKey())  //加密
  .schemaVersion(1)       //指定当前数据库的版本
  .modules(new MySchemaModule())
  .migration(new MyMigration())  //数据库版本不同时的迁移处理
  .build();
```
程序执行以上代码后:

如果是首次执行程序, 那么Realm会创建一个数据库且版本为1.

如果已经存在数据库, 就会进行版本匹配, 并执行迁移操作.

注意`低版本不能覆盖高版本`

#### 2: 数据库需要更新后
```
RealmConfiguration config = new RealmConfiguration.Builder()
  ...
  .schemaVersion(2)
  ...
  .build();
```
程序执行后, 如果之前已经存在数据库, 版本和现在不一样, 那么就会调用MyMigration, 进行数据库的升级迁移操作.

如果:
```
RealmConfiguration config = new RealmConfiguration.Builder()
  ...
  .schemaVersion(3)
  ...
  .build();
```
数据库的版本 有跨度, 比如从1的版本, 直接跨度到3的版本.
那么, 数据库迁移的时候, 仍然需要判断2的版本.

```
public class MyMigration implements RealmMigration {
  @Override
  public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

     // DynamicRealm exposes an editable schema
     RealmSchema schema = realm.getSchema();

     //oldVersion表示当前本地数据库的版本
     if (oldVersion == 1) {
        //版本2 更新的表/字段
        schema.create("Person")
            .addField("name", String.class)
            .addField("age", int.class);
        //更新完成后,
        oldVersion++;//注意此处
     }

     //此时版本数据库版本应该等于2了
     if (oldVersion == 2) {
        //版本3 更新的表/字段
        schema.get("Person")
            .addField("id", long.class, FieldAttribute.PRIMARY_KEY)
            .addRealmObjectField("favoriteDog", schema.get("Dog"))
            .addRealmListField("dogs", schema.get("Dog"));
        oldVersion++;//注意此处
     }
     
     if(oldVersion >= newVersion){
         //迁移完成
         return
     }
  }
}
```
