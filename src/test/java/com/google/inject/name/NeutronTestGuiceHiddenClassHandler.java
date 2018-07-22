package com.google.inject.name;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;

public class NeutronTestGuiceHiddenClassHandler {

  public static void mockBinderNamedImpl(Binder binder) {
    final LinkedBindingBuilder lbb = mock(LinkedBindingBuilder.class);
    mockStatic(Key.class);
    when(binder.bind(Key.get(any(Class.class), any(NamedImpl.class)))).thenReturn(lbb);
  }

}
