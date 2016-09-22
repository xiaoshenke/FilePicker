# FilePicker

FilePicker是一个仿telegram风格文件浏览器，其中某些代码借鉴了telegram。

###使用方法：
1 在你需要展示一个filepicker(文件浏览器)的activity或fragment中定义一个FilePickerImpl。   
2 在你的activity对应的xml文件中定义一个listview。  
3 在onCreate函数中给FilePickerImpl赋值，比如`filePicker = new FilePickerImpl(this,new ListViewProxy(getBaseContext(), (ListView) findViewById(R.id.listView)), mListener);`最后一个参数是IFilePickListener，是选中单个或多个时的回调。              
4 调用filePicker.listRootFiles();搞定！


谢谢 [Telegram](https://github.com/DrKLO/Telegram).
