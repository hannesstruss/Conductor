package com.bluelinelabs.conductor.demo.controllers;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;

import butterknife.BindView;

public class PermissionController extends BaseController {
  @BindView(R.id.btn_request_permission)
  Button btnRequestPermission;

  private boolean permissionListenerSet = false;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_permission, container, false);
  }

  @Override
  protected void onContextAvailable(@NonNull Context context) {
    super.onContextAvailable(context);

    if (!permissionListenerSet) {
      addPermissionResultListener(new PermissionResultListener() {
        @Override
        public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
          Toast.makeText(getActivity(), "Got permission result in listener for request code: " + requestCode, Toast.LENGTH_LONG).show();
        }
      });
      permissionListenerSet = true;
    }
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    btnRequestPermission.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          requestPermissions(new String[]{
              Manifest.permission.ACCESS_FINE_LOCATION}, 124);
        }
      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    Toast.makeText(getActivity(), "Got permission result in controller callback for request code: " + requestCode, Toast.LENGTH_LONG).show();
  }
}
