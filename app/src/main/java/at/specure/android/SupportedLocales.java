package at.specure.android;

import com.specure.opennettest.R;

public enum SupportedLocales {

    DEFAULT(0, null, null, null, R.string.system_pref_lang, 0);


    public String languageCode;
    public String countryCode;
    public String script;
    public int countryIcon;
    public int languageName;
    public int id;



    SupportedLocales(int id, String languageCode, String countryCode, String script, int languageNameResourceId, int flagResourceId) {
        this.id = id;
        this.languageCode = languageCode;
        this.countryCode = countryCode;
        this.countryIcon = flagResourceId;
        this.script = script;
        this.languageName = languageNameResourceId;
    }

    public int getLanguageName() {
        return languageName;
    }
}
