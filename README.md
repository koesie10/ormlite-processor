OrmLite Annotation Processor
===============
[![Build Status](https://travis-ci.org/koesie10/ormlite-processor.svg?branch=master)](https://travis-ci.org/koesie10/ormlite-processor)
[![Download](https://api.bintray.com/packages/koesie10/maven/ormlite-processor/images/download.svg) ](https://bintray.com/koesie10/maven/ormlite-processor/_latestVersion)

An OrmLite annotation processor which helps to cache classes. You may be familiar 
with [OrmLiteConfigUtil](http://ormlite.com/javadoc/ormlite-android/com/j256/ormlite/android/apptools/OrmLiteConfigUtil.html)
which you need to run every time you changed a field or added a table in your Android app. This annotation processor
will do that automatically *at compile-time* and the startup time of your app is diminished even more! There is no need to 
parse an extra file, which means that there is no I/O.

**Note: For now, this project only works on Android. I would like to make it compatible with normal Java as well,
so stay tuned.**

Getting started
--------------
After you've added the dependency, the only thing you'll need to do is call `com.koenv.ormlite.processor.OrmLiteProcessor` when starting up your application.
Call this method before starting to use OrmLite. I would recommend calling it in 
your [Application](https://developer.android.com/reference/android/app/Application.html) class:

```java
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        OrmLiteProcessor.init();
    }
}
```

If you don't want to use Application, call it as early as possible:

```java
public class HelloAndroid extends OrmLiteBaseActivity<DatabaseHelper> {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    // Note how init() is called before onCreate()
	    OrmLiteProcessor.init();
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
		TextView tv = new TextView(this);
		tv.setMovementMethod(new ScrollingMovementMethod());
		doSampleDatabaseStuff("onCreate", tv);
		setContentView(tv);
	}
	
	... // rest of application
}
```

Now that you've added these calls, you can remove `res/raw/ormlite_config.txt` and also remove the reference to it
in your helper by removing the last argument in the super call in your constructor. For example:

```java
public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
}
```
This would become:

```java
public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
}
```

When you now start your application, you should still see a logging call like the following:
```
I/DaoManager(999): Loaded configuration for class ...SimpleData
```
If you see them double however, you haven't removed the old method using files.

Dependency
------------

First, add an annotation processor plugin. I recommend [android-apt](https://bitbucket.org/hvisser/android-apt). Then, add the following snippet:
```java
repositories {
    maven {
        url 'http://dl.bintray.com/koesie10/maven'
    }
}
dependencies {
    apt 'com.koenv:ormlite-processor:0.1'
}
```

Limitations
-----------
* Only one round of processing is supported, which means that the 
[@DatabaseField](http://ormlite.com/javadoc/ormlite-core/com/j256/ormlite/field/DatabaseField.html) 
annotation will give an error if it's found on generated classes.
* Only works on Android because it's hard to detect which database type is going to be used in the code without
a config file.
* It only works for one project in the dependency tree. This is because it always generates the same
class, with the same package and name. If it was used in 2 projects, it would give a duplicated class error.
* No JPA support