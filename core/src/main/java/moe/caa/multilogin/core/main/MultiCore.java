package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.language.LanguageHandler;

public class MultiCore implements MultiLoginAPI {

    @Getter
    private LanguageHandler languageHandler;


}
