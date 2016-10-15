package Examples;

/*
 * Copyright (c) 2015-2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Usage {
    public static void print(Class c, String defaultFilePath) {
    	System.out.println("Usage:");
    	System.out.println(c.getName() + " (no arg)\t: use default test file " 
    			+ (defaultFilePath!=null?defaultFilePath:"(not available)"));
    	System.out.println(c.getName() + " file.mas\t: conduct tests on file.mas");
    	System.out.println(c.getName() + " directory\t: conduct tests on all *.mas files within directory");
    }

}
