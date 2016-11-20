# FilePicker


FilePicker is a [Telegram](https://github.com/DrKLO/Telegram) style file brower,it is very easy to use and integrate to your application.Some code copyed from [Telegram](https://github.com/DrKLO/Telegram).

### Usage：
Step1, declare a FilePickerImpl variable in your Activity/Fragment.   
Step2, init filePickerImpl in onCreate(),mListener is a IFilePickerLiener callback.               
                     
````
private IFilePickerListener mListener = new IFilePickerListener() {
        @Override
        public void onEnterMultiSelectMode() {
        	//yes you can long press to enter multiselect mode
        }
        
        @Override
        public void onQuitMultiSelectMode() {}

        @Override
        public void onFilesSelected(List<String> files) {
        	//do whatever you want
        }
    };
    
filePicker = new FilePickerImpl(this,new ListViewProxy(getBaseContext(), (ListView) findViewById(R.id.listView)), mListener);
````                    
              
Step3, call filePicker.listRootFiles();              
            
````      
filePicker.listRootFiles();
````      
wola! Now you have successfully integrated filepicker to your application! 

Check the code to know more details !
