package net.i2p.android.i2ptunnel;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.i2p.android.router.R;

import java.util.List;

public class TunnelEntryAdapter extends ArrayAdapter<TunnelEntry> {
    private final LayoutInflater mInflater;

    public TunnelEntryAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(List<TunnelEntry> tunnels) {
        clear();
        if (tunnels != null) {
            for (TunnelEntry tunnel : tunnels) {
                add(tunnel);
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.listitem_i2ptunnel, parent, false);
        final TunnelEntry tunnel = getItem(position);

        ImageView status = (ImageView) v.findViewById(R.id.tunnel_status);
        status.setImageDrawable(tunnel.getStatusIcon());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            status.setBackgroundDrawable(tunnel.getStatusBackground());
        else
            status.setBackground(tunnel.getStatusBackground());

        TextView name = (TextView) v.findViewById(R.id.tunnel_name);
        name.setText(tunnel.getName());

        TextView type = (TextView) v.findViewById(R.id.tunnel_description);
        type.setText(tunnel.getDescription());

        TextView ifacePort = (TextView) v.findViewById(R.id.tunnel_interface_port);
        ifacePort.setText(tunnel.getTunnelLink(false));

        if (tunnel.isRunning() && tunnel.isTunnelLinkValid()) {
            View open = v.findViewById(R.id.tunnel_open);
            open.setVisibility(View.VISIBLE);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(tunnel.getTunnelLink(true)));
                    getContext().startActivity(i);
                }
            });
        }

        return v;
    }
}
