#!/bin/sh

# Ensure the keylime user exists
id -u keylime >/dev/null 2>&1 || useradd -r -s /bin/false keylime

# Set the correct ownership for /var/lib/keylime
mkdir -p /var/lib/keylime
chown -R keylime:keylime /var/lib/keylime

echo "Keylime setup completed."
