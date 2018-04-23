package com.andresdlg.groupmeapp.Utils;

import android.app.Application;

/**
 * Created by andresdlg on 17/02/18.
 */


class Helper extends Application {

        private String someVariable;

        public String getSomeVariable() {
            return someVariable;
        }

        public void setSomeVariable(String someVariable) {
            this.someVariable = someVariable;
        }
}
