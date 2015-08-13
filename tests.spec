#!/usr/bin/env rspec
#gem install airborne
#rspec -f d tests.spec

require 'airborne'

token=`cf oauth-token | sed '$!d'`

Airborne.configure do |config|
  config.base_url = ""
  config.headers = {"Authorization" => token}
end

describe 'temporary spec' do
  %w{ /rest/orgs /rest/spaces }.each do |url|
    it "#{url} should return 200" do
      get "#{url}"
      expect_status(200)
    end
  end
end
