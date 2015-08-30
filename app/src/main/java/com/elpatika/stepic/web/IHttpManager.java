package com.elpatika.stepic.web;

import android.os.Bundle;

import java.io.IOException;

public interface IHttpManager {
    String post(String url, Bundle params) throws IOException;
}
